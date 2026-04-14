package com.chargeflow.station.dto;

import com.chargeflow.station.entity.StationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStationRequest(
        @NotBlank
        @Size(max = 150)
        String stationName,

        String locationText,

        @NotNull
        StationStatus status,

        @Size(max = 100)
        String model,

        @Size(max = 100)
        String firmwareVersion
) {
}
