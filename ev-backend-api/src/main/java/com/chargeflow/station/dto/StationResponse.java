package com.chargeflow.station.dto;

import com.chargeflow.station.entity.StationStatus;

import java.time.OffsetDateTime;

public record StationResponse(
        Long id,
        String stationCode,
        String stationName,
        String locationText,
        StationStatus status,
        String model,
        String firmwareVersion,
        String ocppIdentity,
        OffsetDateTime lastSeenAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
