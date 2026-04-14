package com.chargeflow.session_mesurements.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SessionMeasurementResponse(
        Long id,
        Long chargingSessionId,
        OffsetDateTime recordedAt,
        BigDecimal powerKw,
        BigDecimal voltageV,
        BigDecimal currentA,
        Long meterValueWh
) {
}

