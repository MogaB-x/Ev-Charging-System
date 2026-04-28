package com.chargeflow.session_measurements.dto;

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

