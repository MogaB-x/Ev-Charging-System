package com.chargeflow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
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

    public void remoteStartResultIgnored(
            Long sessionId,
            String stationIdentity,
            Integer connectorNumber,
            String reason
    ) {
        LOGGER.warn(
                "CHARGING_SESSION_REMOTE_START_RESULT_IGNORED sessionId={} stationIdentity={} connectorNumber={} reason={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                reason
        );
    }

    public void transactionStartedIgnored(
            Long sessionId,
            String stationIdentity,
            Integer connectorNumber,
            String reason
    ) {
        LOGGER.warn(
                "CHARGING_SESSION_TRANSACTION_STARTED_IGNORED sessionId={} stationIdentity={} connectorNumber={} reason={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                reason
        );
    }

    public void transactionStartedSuccess(Long sessionId, String ocppTransactionId) {
        LOGGER.info(
                "CHARGING_SESSION_TRANSACTION_STARTED_SUCCESS sessionId={} ocppTransactionId={}",
                sessionId,
                ocppTransactionId
        );
    }

    public void transactionStoppedReceived(
            Long sessionId,
            String stationIdentity,
            Integer connectorNumber,
            String ocppTransactionId
    ) {
        LOGGER.info(
                "CHARGING_SESSION_TRANSACTION_STOPPED_RECEIVED sessionId={} stationIdentity={} connectorNumber={} ocppTransactionId={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                ocppTransactionId
        );
    }

    public void transactionStoppedIgnored(
            Long sessionId,
            String stationIdentity,
            Integer connectorNumber,
            String ocppTransactionId,
            String reason
    ) {
        LOGGER.warn(
                "CHARGING_SESSION_TRANSACTION_STOPPED_IGNORED sessionId={} stationIdentity={} connectorNumber={} ocppTransactionId={} reason={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                ocppTransactionId,
                reason
        );
    }

    public void transactionStoppedSuccess(Long sessionId, String ocppTransactionId) {
        LOGGER.info(
                "CHARGING_SESSION_TRANSACTION_STOPPED_SUCCESS sessionId={} ocppTransactionId={}",
                sessionId,
                ocppTransactionId
        );
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
