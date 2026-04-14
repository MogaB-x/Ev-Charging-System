package com.chargeflow.session_mesurements.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateSessionMeasurementRequest(
        @NotNull
        @DecimalMin("0.0")
        BigDecimal powerKw,

        @DecimalMin("0.0")
        BigDecimal voltageV,

        @DecimalMin("0.0")
        BigDecimal currentA,

        @PositiveOrZero
        Long meterValueWh
) {
}
