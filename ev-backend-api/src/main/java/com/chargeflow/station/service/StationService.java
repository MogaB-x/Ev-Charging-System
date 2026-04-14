package com.chargeflow.station.service;

import com.chargeflow.station.dto.CreateStationRequest;
import com.chargeflow.station.dto.StationResponse;
import com.chargeflow.station.dto.UpdateStationRequest;

import java.util.List;

public interface StationService {
    List<StationResponse> getActiveStations();

    StationResponse getStationById(Long stationId);

    StationResponse getStationByOcppIdentity(String ocppIdentity);

    StationResponse createStation(CreateStationRequest request);

    StationResponse updateStation(Long stationId, UpdateStationRequest request);
}
