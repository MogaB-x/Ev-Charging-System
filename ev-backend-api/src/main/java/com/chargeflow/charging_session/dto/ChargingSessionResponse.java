package com.chargeflow.charging_session.dto;

import com.chargeflow.charging_session.entity.ChargingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ChargingSessionResponse(
        Long id,
        String sessionCode,
        Long stationId,
        Long connectorId,
        ChargingStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        BigDecimal energyConsumedKwh,
        BigDecimal totalPrice
) {
}
