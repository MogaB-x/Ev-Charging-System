package com.chargeflow.session_mesurements.dto;

import com.chargeflow.charging_session.entity.ChargingStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateChargingSessionStatusRequest(
        @NotNull
        ChargingStatus status,

        @Size(max = 100)
        String stopReason
) {
}

