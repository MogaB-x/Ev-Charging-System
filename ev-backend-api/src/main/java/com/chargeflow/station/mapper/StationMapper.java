package com.chargeflow.station.mapper;

import com.chargeflow.station.dto.CreateStationRequest;
import com.chargeflow.station.dto.StationResponse;
import com.chargeflow.station.dto.UpdateStationRequest;
import com.chargeflow.station.entity.Station;

public final class StationMapper {

    private StationMapper(){
    }

    public static Station toEntity(CreateStationRequest request) {
        Station station = new Station();
        station.setStationCode(request.stationCode());
        station.setStationName(request.stationName());
        station.setLocationText(request.locationText());
        station.setStatus(request.status());
        station.setModel(request.model());
        station.setFirmwareVersion(request.firmwareVersion());
        station.setOcppIdentity(request.ocppIdentity());
        return station;
    }

    public static void updateEntity(Station station, UpdateStationRequest request) {
        station.setStationName(request.stationName());
        station.setLocationText(request.locationText());
        station.setStatus(request.status());
        station.setModel(request.model());
        station.setFirmwareVersion(request.firmwareVersion());
    }

    public static StationResponse toResponse(Station station) {
        return new StationResponse(
                station.getId(),
                station.getStationCode(),
                station.getStationName(),
                station.getLocationText(),
                station.getStatus(),
                station.getModel(),
                station.getFirmwareVersion(),
                station.getOcppIdentity(),
                station.getLastSeenAt(),
                station.getCreatedAt(),
                station.getUpdatedAt()
        );
    }

}
