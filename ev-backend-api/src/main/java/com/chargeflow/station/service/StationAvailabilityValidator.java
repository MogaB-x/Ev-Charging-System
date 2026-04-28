package com.chargeflow.station.service;

import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.station.entity.Station;
import com.chargeflow.station.entity.StationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;

@Component
public class StationAvailabilityValidator {

    private final Duration presenceTimeout;

    public StationAvailabilityValidator(@Value("${station.presence-timeout-seconds:180}") long presenceTimeoutSeconds) {
        this.presenceTimeout = Duration.ofSeconds(presenceTimeoutSeconds);
    }

    public void validateStationIsOnline(Station station) {
        OffsetDateTime lastSeenAt = station.getLastSeenAt();
        OffsetDateTime onlineThreshold = OffsetDateTime.now().minus(presenceTimeout);

        if (lastSeenAt == null || lastSeenAt.isBefore(onlineThreshold)) {
            throw new ConflictException("Station is offline");
        }
    }

    public void validateStationIsOperational(Station station) {
        if (station.getStatus() != StationStatus.AVAILABLE) {
            throw new ConflictException("Station is not available for charging");
        }
    }
}
