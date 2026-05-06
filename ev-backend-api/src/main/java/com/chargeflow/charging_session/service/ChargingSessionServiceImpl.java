package com.chargeflow.charging_session.service;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.messaging.contract.command.RemoteStartCommand;
import com.chargeflow.messaging.contract.event.RemoteStartResultEvent;
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
import java.util.Optional;

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
    private final ChargingSessionEventValidator chargingSessionEventValidator;

    private static final EnumSet<ChargingStatus> ACTIVE_STATUSES =
            EnumSet.of(ChargingStatus.PENDING, ChargingStatus.IN_PROGRESS);

    @Override
    public ChargingSessionResponse startSession(String userEmail, StartSessionRequest request) {
        User user = findUserByEmail(userEmail);

        Station station = findStationById(request.stationId());

        Connector connector = findConnectorByStationAndNumber(station.getId(), request.connectorNumber());

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
    public void handleRemoteStartResultEvent(RemoteStartResultEvent event) {
        chargingSessionAuditLogger.remoteStartResultReceived(event.getSessionId(), event.getResult());
        if (event.getSessionId() == null) {
            logRemoteStartResultIgnored(event, "Missing session id");
            return;
        }

        ChargingSession session = findSessionByIdOrNull(event.getSessionId());
        if (session == null) {
            logRemoteStartResultIgnored(event, "Charging session was not found");
            return;
        }

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        Optional<String> remoteStartResultValidation =
                chargingSessionEventValidator.validateRemoteStartResult(session, event);
        if (remoteStartResultValidation.isPresent()) {
            logRemoteStartResultIgnored(event, remoteStartResultValidation.get());
            return;
        }

        if ("REJECTED".equals(event.getResult())) {
            handleRejectedRemoteStartResult(session, event);
            return;
        }

        if ("ACCEPTED".equals(event.getResult())) {
            handleAcceptedRemoteStartResult(session);
        }
    }

    public void handleTransactionStartedEvent(TransactionStartedEvent event){
        if (event.getSessionId() == null) {
            logTransactionStartedIgnored(event, "Missing session id");
            return;
        }

        ChargingSession session = findSessionByIdOrNull(event.getSessionId());

        if (session == null) {
            logTransactionStartedIgnored(event, "Charging session was not found");
            return;
        }

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        Optional<String> transactionStartedValidation =
                chargingSessionEventValidator.validateTransactionStarted(session, event);
        if (transactionStartedValidation.isPresent()) {
            logTransactionStartedIgnored(event, transactionStartedValidation.get());
            return;
        }

        if (event.getMeterStartWh() != null && event.getMeterStartWh() < 0) {
            logTransactionStartedIgnored(event, "Invalid meter start value");
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
            logTransactionStoppedIgnored(event, "Missing session id");
            return;
        }

        ChargingSession session = findSessionByIdOrNull(event.getSessionId());

        if (session == null) {
            logTransactionStoppedIgnored(event, "Charging session was not found");
            return;
        }

        Optional<String> transactionStoppedValidation =
                chargingSessionEventValidator.validateTransactionStopped(session, event);
        if (transactionStoppedValidation.isPresent()) {
            logTransactionStoppedIgnored(event, transactionStoppedValidation.get());
            return;
        }

        if (event.getMeterStopWh() == null) {
            logTransactionStoppedIgnored(event, "Missing meter stop value");
            return;
        }

        if (event.getMeterStopWh() < 0) {
            logTransactionStoppedIgnored(event, "Invalid meter stop value");
            return;
        }

        if (session.getMeterStopWh() != null && event.getMeterStopWh() < session.getMeterStopWh()) {
            logTransactionStoppedIgnored(event, "Meter stop value is lower than the latest known session meter value");
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
        ChargingSession session = findSessionByIdAndUserEmail(sessionId, userEmail);

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
        ChargingSession session = findSessionByIdAndUserEmail(sessionId, userEmail);

        chargingSessionLifecycleHelper.expireTimedOutPendingSession(session);

        return ChargingSessionMapper.toResponse(session);
    }

    private User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Station findStationById(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station not found"));
    }

    private Connector findConnectorByStationAndNumber(Long stationId, Integer connectorNumber) {
        return connectorRepository.findByStationIdAndConnectorNumber(stationId, connectorNumber)
                .orElseThrow(() -> new NotFoundException("Connector not found for station and connector number"));
    }

    private ChargingSession findSessionByIdOrNull(Long sessionId) {
        return sessionRepository.findById(sessionId).orElse(null);
    }

    private ChargingSession findSessionByIdAndUserEmail(Long sessionId, String userEmail) {
        return sessionRepository.findByIdAndUserEmail(sessionId, userEmail)
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    private void handleRejectedRemoteStartResult(ChargingSession session, RemoteStartResultEvent event) {
        chargingSessionAuditLogger.remoteStartRejected(event.getSessionId(), event.getReason());
        String stopReason = StringUtils.hasText(event.getReason())
                ? event.getReason().trim()
                : "Remote start rejected by station";

        chargingSessionLifecycleHelper.applyStatusTransition(
                session,
                ChargingStatus.FAILED,
                stopReason
        );
    }

    private void handleAcceptedRemoteStartResult(ChargingSession session) {
        chargingSessionAuditLogger.remoteStartAccepted(session.getId());
    }

    private void logRemoteStartResultIgnored(RemoteStartResultEvent event, String reason) {
        chargingSessionAuditLogger.remoteStartResultIgnored(
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber(),
                reason
        );
    }

    private void logTransactionStartedIgnored(TransactionStartedEvent event, String reason) {
        chargingSessionAuditLogger.transactionStartedIgnored(
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber(),
                reason
        );
    }

    private void logTransactionStoppedIgnored(TransactionStoppedEvent event, String reason) {
        chargingSessionAuditLogger.transactionStoppedIgnored(
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber(),
                event.getOcppTransactionId(),
                reason
        );
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
