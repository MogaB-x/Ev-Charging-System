package com.chargeflow.station.controller;

import com.chargeflow.station.dto.StationResponse;
import com.chargeflow.station.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/stations")
@RequiredArgsConstructor
public class InternalStationController {

    private final StationService stationService;

    @GetMapping("/by-ocpp-identity/{ocppIdentity}")
    public StationResponse getStationByOcppIdentity(@PathVariable String ocppIdentity) {
        return stationService.getStationByOcppIdentity(ocppIdentity);
    }
}

