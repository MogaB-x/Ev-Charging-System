package com.chargeflow.connector.dto;

import com.chargeflow.connector.entity.ConnectorStatus;
import com.chargeflow.connector.entity.ConnectorType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateConnectorRequest(
        @NotNull
        @Positive
        Integer connectorNumber,

        @NotNull
        ConnectorType connectorType,

        @NotNull
        ConnectorStatus connectorStatus,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal maxPowerKw,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal pricePerKw
) {
}
