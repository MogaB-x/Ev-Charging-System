package com.chargeflow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class SessionMeasurementAuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionMeasurementAuditLogger.class);

    public void meterValuesReceived(Long sessionId, String stationIdentity, Integer connectorNumber, String ocppTransactionId) {
        LOGGER.info(
                "SESSION_MEASUREMENT_METER_VALUES_RECEIVED sessionId={} stationIdentity={} connectorNumber={} ocppTransactionId={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                ocppTransactionId
        );
    }

    public void meterValuesIgnored(
            Long sessionId,
            String stationIdentity,
            Integer connectorNumber,
            String ocppTransactionId,
            String reason
    ) {
        LOGGER.warn(
                "SESSION_MEASUREMENT_METER_VALUES_IGNORED sessionId={} stationIdentity={} connectorNumber={} ocppTransactionId={} reason={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                ocppTransactionId,
                reason
        );
    }

    public void meterValuesProcessed(
            Long sessionId,
            String stationIdentity,
            Integer connectorNumber,
            String ocppTransactionId,
            Long meterValueWh
    ) {
        LOGGER.info(
                "SESSION_MEASUREMENT_METER_VALUES_PROCESSED sessionId={} stationIdentity={} connectorNumber={} ocppTransactionId={} meterValueWh={}",
                sessionId,
                stationIdentity,
                connectorNumber,
                ocppTransactionId,
                meterValueWh
        );
    }

    public void unexpectedError(String action, String reference, Exception ex) {
        LOGGER.error(
                "SESSION_MEASUREMENT_UNEXPECTED_ERROR action={} reference={} message={}",
                action,
                reference,
                ex.getMessage(),
                ex
        );
    }
}
