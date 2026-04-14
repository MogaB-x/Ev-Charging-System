package com.chargeflow.charging_session.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartSessionRequest(
        @NotNull Long stationId,
        @NotNull
        @Positive
        Integer connectorNumber
) {
}
