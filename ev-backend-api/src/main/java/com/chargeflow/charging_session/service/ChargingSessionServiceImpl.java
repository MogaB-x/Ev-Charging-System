package com.chargeflow.charging_session.service;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.messaging.contract.command.RemoteStartCommand;
import com.chargeflow.charging_session.dto.RemoteStartResultEvent;
import com.chargeflow.charging_session.dto.StartSessionRequest;
import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.charging_session.mapper.ChargingSessionMapper;
import com.chargeflow.charging_session.repository.ChargingSessionRepository;
import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.common.exception.NotFoundException;
import com.chargeflow.connector.entity.Connector;
import com.chargeflow.connector.entity.ConnectorStatus;
import com.chargeflow.connector.repository.ConnectorRepository;
import com.chargeflow.logger.ChargingSessionAuditLogger;
import com.chargeflow.messaging.RemoteStartCommandPublisher;
import com.chargeflow.messaging.contract.event.TransactionStartedEvent;
import com.chargeflow.messaging.contract.event.TransactionStoppedEvent;
import com.chargeflow.station.entity.Station;
import com.chargeflow.station.repository.StationRepository;
import com.chargeflow.station.service.StationAvailabilityValidator;
import com.chargeflow.user.entity.User;
import com.chargeflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ChargingSessionServiceImpl implements ChargingSessionService{

    private final ChargingSessionRepository sessionRepository;
    private final StationRepository stationRepository;
    private final ConnectorRepository connectorRepository;
    private final UserRepository userRepository;
    private final RemoteStartCommandPublisher remoteStartCommandPublisher;
    private final ChargingSessionAuditLogger chargingSessionAuditLogger;
    private final StationAvailabilityValidator stationAvailabilityValidator;
    private final ChargingSessionLifecycleHelper chargingSessionLifecycleHelper;

    private static final EnumSet<ChargingStatus> ACTIVE_STATUSES =
            EnumSet.of(ChargingStatus.PENDING, ChargingStatus.IN_PROGRESS);

    @Override
    public ChargingSessionResponse startSession(String userEmail, StartSessionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Station station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new NotFoundException("Station not found"));

        Connector connector = connectorRepository
                .findByStationIdAndConnectorNumber(station.getId(), request.connectorNumber())
                .orElseThrow(() -> new NotFoundException("Connector not found for station and connector number"));

        if (!connector.getStation().getId().equals(station.getId())) {
            throw new ConflictException("Connector does not belong to station");
        }

        chargingSessionLifecycleHelper.expireTimedOutPendingSessionsForUser(userEmail);
        chargingSessionLifecycleHelper.expireTimedOutPendingSession(
                sessionRepository.findByConnectorIdAndStatus(connector.getId(), ChargingStatus.PENDING)
                .orElse(null));

        validateStationIsKnown(station);
        stationAvailabilityValidator.validateStationIsOnline(station);
        stationAvailabilityValidator.validateStationIsOperational(station);

        if (connector.getConnectorStatus() != ConnectorStatus.AVAILABLE) {
            throw new ConflictException("Connector not available");
        }

        if (sessionRepository.existsByUserEmailAndStatusIn(userEmail, ACTIVE_STATUSES)) {
            throw new ConflictException("User already has an active session");
        }

        if (user.getBalance().compareTo(BigDecimal.valueOf(10)) < 0) {
            user.setBalance(user.getBalance().add(BigDecimal.valueOf(50)));
        }

        ChargingSession session = ChargingSessionMapper.toEntity(user, station, connector);
        connector.setConnectorStatus(ConnectorStatus.PREPARING);

        ChargingSession saved = sessionRepository.save(session);

        remoteStartCommandPublisher.publish(buildRemoteStartCommand(saved));

        return ChargingSessionMapper.toResponse(saved);
    }

    @Transactional
    public void handleRemoteStartResult(RemoteStartResultEvent event) {
        chargingSessionAuditLogger.remoteStartResultReceived(event.getSessionId(), event.getResult());
        ChargingSession session = sessionRepository.findById(event.getSessionId())
                .orElseThrow(() -> new NotFoundException("Session not found"));

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        if (session.getStatus() != ChargingStatus.PENDING) {
            return;
        }

        if ("REJECTED".equals(event.getResult())) {
            chargingSessionAuditLogger.remoteStartRejected(event.getSessionId(), event.getReason());
            session.setStatus(ChargingStatus.FAILED);
            session.setStopReason(event.getReason());

            Connector connector = session.getConnector();
            connector.setConnectorStatus(ConnectorStatus.AVAILABLE);

            sessionRepository.save(session);
            return;
        }

        if ("ACCEPTED".equals(event.getResult())) {
            chargingSessionAuditLogger.remoteStartAccepted(event.getSessionId());
            sessionRepository.save(session);
        }
    }

    public void handleTransactionStartedEvent(TransactionStartedEvent event){
        if (event.getSessionId() == null) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    null,
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Missing session id"
            );
            return;
        }

        ChargingSession session = sessionRepository.findById(event.getSessionId())
                .orElse(null);

        if (session == null) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Charging session was not found"
            );
            return;
        }

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        if (session.getStatus() != ChargingStatus.PENDING) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Session is no longer pending"
            );
            return;
        }

        if (!Objects.equals(event.getStationIdentity(), session.getStation().getOcppIdentity())) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Station identity does not match the pending session"
            );
            return;
        }

        if (!Objects.equals(event.getConnectorNumber(), session.getConnector().getConnectorNumber())) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Connector number does not match the pending session"
            );
            return;
        }

        if (!StringUtils.hasText(event.getOcppTransactionId())) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Missing OCPP transaction id"
            );
            return;
        }

        if (event.getMeterStartWh() != null && event.getMeterStartWh() < 0) {
            chargingSessionAuditLogger.transactionStartedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    "Invalid meter start value"
            );
            return;
        }

        chargingSessionLifecycleHelper.startPendingSession(
                session,
                OffsetDateTime.now(),
                event.getOcppTransactionId().trim(),
                event.getMeterStartWh()
        );
        chargingSessionAuditLogger.transactionStartedSuccess(session.getId(), session.getOcppTransactionId());
    }

    public void handleTransactionStoppedEvent(TransactionStoppedEvent event) {
        chargingSessionAuditLogger.transactionStoppedReceived(
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber(),
                event.getOcppTransactionId()
        );

        if (event.getSessionId() == null) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    null,
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Missing session id"
            );
            return;
        }

        ChargingSession session = sessionRepository.findById(event.getSessionId())
                .orElse(null);

        if (session == null) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Charging session was not found"
            );
            return;
        }

        if (session.getStatus() != ChargingStatus.IN_PROGRESS) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Charging session is not in progress"
            );
            return;
        }

        if (!Objects.equals(event.getStationIdentity(), session.getStation().getOcppIdentity())) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Station identity does not match the charging session"
            );
            return;
        }

        if (!Objects.equals(event.getConnectorNumber(), session.getConnector().getConnectorNumber())) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Connector number does not match the charging session"
            );
            return;
        }

        if (!Objects.equals(event.getOcppTransactionId(), session.getOcppTransactionId())) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "OCPP transaction id does not match the charging session"
            );
            return;
        }

        if (event.getMeterStopWh() == null) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Missing meter stop value"
            );
            return;
        }

        if (event.getMeterStopWh() < 0) {
            chargingSessionAuditLogger.transactionStoppedIgnored(
                    event.getSessionId(),
                    event.getStationIdentity(),
                    event.getConnectorNumber(),
                    event.getOcppTransactionId(),
                    "Invalid meter stop value"
            );
            return;
        }

        session.setMeterStopWh(event.getMeterStopWh());

        String stopReason = StringUtils.hasText(event.getStopReason())
                ? event.getStopReason().trim()
                : "Charging session stopped by station";

        chargingSessionLifecycleHelper.applyStatusTransition(
                session,
                ChargingStatus.COMPLETED,
                stopReason
        );

        chargingSessionAuditLogger.transactionStoppedSuccess(session.getId(), session.getOcppTransactionId());
    }


    @Override
    public ChargingSessionResponse stopSession(String userEmail, Long sessionId) {
        ChargingSession session = sessionRepository.findByIdAndUserEmail(sessionId, userEmail)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        if (session.getStatus() != ChargingStatus.PENDING && session.getStatus() != ChargingStatus.IN_PROGRESS) {
            throw new ConflictException("Only pending or active sessions can be stopped");
        }

        ChargingStatus requestedStatus = session.getStatus() == ChargingStatus.PENDING
                ? ChargingStatus.CANCELLED
                : ChargingStatus.COMPLETED;
        String stopReason = session.getStatus() == ChargingStatus.PENDING
                ? "User cancelled pending charging session"
                : "User manually stopped charging";

        ChargingSession finalized = chargingSessionLifecycleHelper.applyStatusTransition(
                session,
                requestedStatus,
                stopReason
        );

        return ChargingSessionMapper.toResponse(finalized);
    }

    @Override
    public List<ChargingSessionResponse> getMySessions(String userEmail) {
        chargingSessionLifecycleHelper.expireTimedOutPendingSessionsForUser(userEmail);

        return sessionRepository.findByUserEmail(userEmail)
                .stream()
                .map(ChargingSessionMapper::toResponse)
                .toList();
    }

    @Override
    public ChargingSessionResponse getSessionById(String userEmail, Long sessionId) {
        ChargingSession session = sessionRepository.findByIdAndUserEmail(sessionId, userEmail)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        return ChargingSessionMapper.toResponse(session);
    }

    private void validateStationIsKnown(Station station) {
        if (!StringUtils.hasText(station.getOcppIdentity())) {
            throw new ConflictException("Station is not known by the charging gateway");
        }
    }

    private RemoteStartCommand buildRemoteStartCommand(ChargingSession session) {
        return new RemoteStartCommand(
                session.getId(),
                session.getSessionCode(),
                session.getStation().getOcppIdentity(),
                session.getConnector().getConnectorNumber(),
                session.getCreatedAt()
        );
    }
}
