package com.chargeflow.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StationAuditLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationAuditLogger.class);

    public void createAttempt(String stationCode) {
        LOGGER.info("STATION_CREATE_ATTEMPT stationCode={}", stationCode);
    }

    public void createSuccess(Long stationId, String stationCode) {
        LOGGER.info("STATION_CREATE_SUCCESS stationId={} stationCode={}", stationId, stationCode);
    }

    public void createFailure(String stationCode, String reason) {
        LOGGER.warn("STATION_CREATE_FAILURE stationCode={} reason={}", stationCode, reason);
    }

    public void updateAttempt(Long stationId) {
        LOGGER.info("STATION_UPDATE_ATTEMPT stationId={}", stationId);
    }

    public void updateSuccess(Long stationId, String stationCode) {
        LOGGER.info("STATION_UPDATE_SUCCESS stationId={} stationCode={}", stationId, stationCode);
    }

    public void updateFailure(Long stationId, String reason) {
        LOGGER.warn("STATION_UPDATE_FAILURE stationId={} reason={}", stationId, reason);
    }

    public void fetchByIdFailure(Long stationId, String reason) {
        LOGGER.warn("STATION_FETCH_BY_ID_FAILURE stationId={} reason={}", stationId, reason);
    }

    public void fetchByOcppIdentityFailure(String ocppIdentity, String reason) {
        LOGGER.warn("STATION_FETCH_BY_OCPP_IDENTITY_FAILURE ocppIdentity={} reason={}", ocppIdentity, reason);
    }

    public void bootNotificationFailure(String ocppIdentity, String reason) {
        LOGGER.warn("STATION_BOOT_NOTIFICATION_FAILURE ocppIdentity={} reason={}", ocppIdentity, reason);
    }

    public void bootNotificationSuccess(Long stationId, String stationCode, String ocppIdentity) {
        LOGGER.info(
                "STATION_BOOT_NOTIFICATION_SUCCESS stationId={} stationCode={} ocppIdentity={}",
                stationId,
                stationCode,
                ocppIdentity
        );
    }

    public void unexpectedError(String action, String reference, Exception ex) {
        LOGGER.error("STATION_UNEXPECTED_ERROR action={} reference={} message={}", action, reference, ex.getMessage(), ex);
    }
}

