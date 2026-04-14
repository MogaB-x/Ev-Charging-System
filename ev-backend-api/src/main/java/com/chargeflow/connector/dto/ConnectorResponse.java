package com.chargeflow.connector.dto;

import com.chargeflow.connector.entity.ConnectorStatus;
import com.chargeflow.connector.entity.ConnectorType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ConnectorResponse(
        Long id,
        Long stationId,
        Integer connectorNumber,
        ConnectorType connectorType,
        ConnectorStatus connectorStatus,
        BigDecimal maxPowerKw,
        BigDecimal pricePerKw,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
