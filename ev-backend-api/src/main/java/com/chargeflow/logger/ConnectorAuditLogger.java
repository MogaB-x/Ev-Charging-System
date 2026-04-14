package com.chargeflow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("unused")
public class ConnectorAuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorAuditLogger.class);

    public void createAttempt(Long stationId, Integer connectorNumber) {
        LOGGER.info("CONNECTOR_CREATE_ATTEMPT stationId={} connectorNumber={}", stationId, connectorNumber);
    }

    public void createSuccess(Long connectorId, Long stationId, Integer connectorNumber) {
        LOGGER.info(
                "CONNECTOR_CREATE_SUCCESS connectorId={} stationId={} connectorNumber={}",
                connectorId,
                stationId,
                connectorNumber
        );
    }

    public void createFailure(Long stationId, Integer connectorNumber, String reason) {
        LOGGER.warn(
                "CONNECTOR_CREATE_FAILURE stationId={} connectorNumber={} reason={}",
                stationId,
                connectorNumber,
                reason
        );
    }

    public void updateAttempt(Long connectorId) {
        LOGGER.info("CONNECTOR_UPDATE_ATTEMPT connectorId={}", connectorId);
    }

    public void updateSuccess(Long connectorId, Long stationId, Integer connectorNumber) {
        LOGGER.info(
                "CONNECTOR_UPDATE_SUCCESS connectorId={} stationId={} connectorNumber={}",
                connectorId,
                stationId,
                connectorNumber
        );
    }

    public void updateFailure(Long connectorId, String reason) {
        LOGGER.warn("CONNECTOR_UPDATE_FAILURE connectorId={} reason={}", connectorId, reason);
    }

    public void fetchByIdFailure(Long connectorId, String reason) {
        LOGGER.warn("CONNECTOR_FETCH_BY_ID_FAILURE connectorId={} reason={}", connectorId, reason);
    }

    public void fetchByStationFailure(Long stationId, String reason) {
        LOGGER.warn("CONNECTOR_FETCH_BY_STATION_FAILURE stationId={} reason={}", stationId, reason);
    }

    public void unexpectedError(String action, String reference, Exception ex) {
        LOGGER.error(
                "CONNECTOR_UNEXPECTED_ERROR action={} reference={} message={}",
                action,
                reference,
                ex.getMessage(),
                ex
        );
    }
}


