package com.chargeflow.charging_session.service;

import com.chargeflow.charging_session.calculator.ChargingSessionCalculator;
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

    private static final EnumSet<ChargingStatus> ACTIVE_STATUSES =
            EnumSet.of(ChargingStatus.PENDING, ChargingStatus.IN_PROGRESS);
    private static final EnumSet<ChargingStatus> TERMINAL_STATUSES =
            EnumSet.of(ChargingStatus.COMPLETED, ChargingStatus.FAILED, ChargingStatus.CANCELLED);
    private final ChargingSessionCalculator chargingSessionCalculator;

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

    @Override
    public ChargingSessionResponse stopSession(String userEmail, Long sessionId) {
        ChargingSession session = sessionRepository.findByIdAndUserEmail(sessionId, userEmail)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        ChargingSession finalized = finalizeSession(
                session,
                ChargingStatus.COMPLETED,
                "User manually stopped charging"
        );

        return ChargingSessionMapper.toResponse(finalized);
    }

    @Override
    public List<ChargingSessionResponse> getMySessions(String userEmail) {
        return sessionRepository.findByUserEmail(userEmail)
                .stream()
                .map(ChargingSessionMapper::toResponse)
                .toList();
    }

    @Override
    public ChargingSessionResponse getSessionById(String userEmail, Long sessionId) {
        ChargingSession session = sessionRepository.findByIdAndUserEmail(sessionId, userEmail)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        return ChargingSessionMapper.toResponse(session);
    }

    @Override
    public ChargingSessionResponse finalizeSessionInternal(Long sessionId, ChargingStatus status, String stopReason) {
        ChargingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Charging session with id " + sessionId + " was not found"));

        ChargingSession finalized = finalizeSession(session, status, stopReason);

        return ChargingSessionMapper.toResponse(finalized);
    }

    //helpers
    private ChargingSession finalizeSession(
            ChargingSession session,
            ChargingStatus newStatus,
            String stopReason
    ) {

        validateStatusTransition(session.getStatus(), newStatus);

        if (session.getStatus() != ChargingStatus.IN_PROGRESS) {
            throw new ConflictException("Session is not active");
        }

        session.setStatus(newStatus);
        session.setStopReason(stopReason);

        if (session.getEndedAt() == null) {
            session.setEndedAt(OffsetDateTime.now());
        }

        chargingSessionCalculator.recalculateSessionTotals(session);

        if (newStatus == ChargingStatus.COMPLETED && session.getTotalPrice() != null) {
            User user = session.getUser();
            user.setBalance(user.getBalance().subtract(session.getTotalPrice()));
        }

        session.getConnector().setConnectorStatus(ConnectorStatus.AVAILABLE);

        return sessionRepository.save(session);
    }

    private void validateStatusTransition(ChargingStatus currentStatus, ChargingStatus requestedStatus) {
        if (currentStatus == requestedStatus) {
            return;
        }

        if (currentStatus != ChargingStatus.IN_PROGRESS) {
            throw new ConflictException("Only active sessions can be finalized");
        }

        if (TERMINAL_STATUSES.contains(currentStatus)) {
            throw new ConflictException("Cannot change status of a finished charging session");
        }

        if (requestedStatus == ChargingStatus.PENDING) {
            throw new ConflictException("Charging session status cannot transition back to PENDING");
        }
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
