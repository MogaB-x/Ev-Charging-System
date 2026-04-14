package com.chargeflow.admin.controller;

import com.chargeflow.station.dto.CreateStationRequest;
import com.chargeflow.station.dto.StationResponse;
import com.chargeflow.station.dto.UpdateStationRequest;
import com.chargeflow.station.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/stations")
@RequiredArgsConstructor
public class AdminStationController {

    private final StationService stationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse createStation(@Valid @RequestBody CreateStationRequest request) {
        return stationService.createStation(request);
    }

    @PutMapping("/{stationId}")
    public StationResponse updateStation(
            @PathVariable Long stationId,
            @Valid @RequestBody UpdateStationRequest request
    ) {
        return stationService.updateStation(stationId, request);
    }
}