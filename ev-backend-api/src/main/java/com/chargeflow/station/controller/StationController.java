package com.chargeflow.station.controller;

import com.chargeflow.station.dto.StationResponse;
import com.chargeflow.station.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public List<StationResponse> getActiveStations() {
        return stationService.getActiveStations();
    }

    @GetMapping("/{stationId}")
    public StationResponse getStationById(@PathVariable Long stationId) {
        return stationService.getStationById(stationId);
    }
}
