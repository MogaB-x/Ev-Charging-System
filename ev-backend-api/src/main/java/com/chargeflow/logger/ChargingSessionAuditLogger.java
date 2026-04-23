package com.chargeflow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class ChargingSessionAuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChargingSessionAuditLogger.class);

    public void remoteStartResultReceived(Long sessionId, String result) {
        LOGGER.info("CHARGING_SESSION_REMOTE_START_RESULT_RECEIVED sessionId={} result={}", sessionId, result);
    }

    public void remoteStartRejected(Long sessionId, String reason) {
        LOGGER.info("CHARGING_SESSION_REMOTE_START_REJECTED sessionId={} reason={}", sessionId, reason);
    }

    public void remoteStartAccepted(Long sessionId) {
        LOGGER.info("CHARGING_SESSION_REMOTE_START_ACCEPTED sessionId={}", sessionId);
    }

    public void unexpectedError(String action, String reference, Exception ex) {
        LOGGER.error(
                "CHARGING_SESSION_UNEXPECTED_ERROR action={} reference={} message={}",
                action,
                reference,
                ex.getMessage(),
                ex
        );
    }
}
