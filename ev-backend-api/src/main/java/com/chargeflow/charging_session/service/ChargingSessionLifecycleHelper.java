package com.chargeflow.charging_session.service;

import com.chargeflow.charging_session.calculator.ChargingSessionCalculator;
import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.charging_session.repository.ChargingSessionRepository;
import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.connector.entity.ConnectorStatus;
import com.chargeflow.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.EnumSet;

@Component
public class ChargingSessionLifecycleHelper {

    private static final EnumSet<ChargingStatus> TERMINAL_STATUSES =
            EnumSet.of(ChargingStatus.COMPLETED, ChargingStatus.FAILED, ChargingStatus.CANCELLED);

    private final ChargingSessionRepository sessionRepository;
    private final ChargingSessionCalculator chargingSessionCalculator;
    private final long pendingTimeoutSeconds;

    public ChargingSessionLifecycleHelper(
            ChargingSessionRepository sessionRepository,
            ChargingSessionCalculator chargingSessionCalculator,
            @Value("${charging.session.pending-timeout-seconds:300}") long pendingTimeoutSeconds
    ) {
        this.sessionRepository = sessionRepository;
        this.chargingSessionCalculator = chargingSessionCalculator;
        this.pendingTimeoutSeconds = pendingTimeoutSeconds;
    }

    public ChargingSession applyStatusTransition(
            ChargingSession session,
            ChargingStatus newStatus,
            String stopReason
    ) {
        if (session.getStatus() == newStatus) {
            return session;
        }

        if (session.getStatus() == ChargingStatus.PENDING) {
            return transitionPendingSession(session, newStatus, stopReason);
        }

        if (session.getStatus() == ChargingStatus.IN_PROGRESS) {
            return finalizeActiveSession(session, newStatus, stopReason);
        }

        throw new ConflictException("Cannot change status of a finished charging session");
    }

    public void expireTimedOutPendingSessionsForUser(String userEmail) {
        sessionRepository.findByUserEmailAndStatus(userEmail, ChargingStatus.PENDING)
                .forEach(this::expireTimedOutPendingSession);
    }

    public void expireTimedOutPendingSession(ChargingSession session) {
        if (session == null || session.getStatus() != ChargingStatus.PENDING) {
            return;
        }

        if (!isPendingTimedOut(session)) {
            return;
        }

        session.setStatus(ChargingStatus.FAILED);
        session.setStopReason("Remote start confirmation timed out");
        session.getConnector().setConnectorStatus(ConnectorStatus.AVAILABLE);
        sessionRepository.save(session);
    }

    private ChargingSession transitionPendingSession(
            ChargingSession session,
            ChargingStatus newStatus,
            String stopReason
    ) {
        if (newStatus == ChargingStatus.IN_PROGRESS) {
            session.setStatus(ChargingStatus.IN_PROGRESS);
            if (session.getStartedAt() == null) {
                session.setStartedAt(OffsetDateTime.now());
            }
            session.getConnector().setConnectorStatus(ConnectorStatus.CHARGING);
            return sessionRepository.save(session);
        }

        if (newStatus == ChargingStatus.FAILED || newStatus == ChargingStatus.CANCELLED) {
            session.setStatus(newStatus);
            session.setStopReason(stopReason);
            session.getConnector().setConnectorStatus(ConnectorStatus.AVAILABLE);
            return sessionRepository.save(session);
        }

        throw new ConflictException("Pending session can only transition to IN_PROGRESS, FAILED or CANCELLED");
    }

    private ChargingSession finalizeActiveSession(
            ChargingSession session,
            ChargingStatus newStatus,
            String stopReason
    ) {
        validateFinalStatus(newStatus);

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

    private void validateFinalStatus(ChargingStatus requestedStatus) {
        if (!TERMINAL_STATUSES.contains(requestedStatus)) {
            throw new ConflictException("Active session can only transition to COMPLETED, FAILED or CANCELLED");
        }
    }

    private boolean isPendingTimedOut(ChargingSession session) {
        OffsetDateTime pendingDeadline = session.getCreatedAt()
                .plus(Duration.ofSeconds(pendingTimeoutSeconds));

        return OffsetDateTime.now().isAfter(pendingDeadline);
    }
}
