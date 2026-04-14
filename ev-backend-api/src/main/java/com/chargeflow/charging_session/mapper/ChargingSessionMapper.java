package com.chargeflow.charging_session.mapper;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.connector.entity.Connector;
import com.chargeflow.station.entity.Station;
import com.chargeflow.user.entity.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ChargingSessionMapper {
    private ChargingSessionMapper() {}

    public static ChargingSession toEntity(User user, Station station, Connector connector) {
        ChargingSession session = new ChargingSession();
        session.setSessionCode(UUID.randomUUID().toString());
        session.setUser(user);
        session.setStation(station);
        session.setConnector(connector);
        session.setStatus(ChargingStatus.IN_PROGRESS);
        session.setStartedAt(OffsetDateTime.now());
        session.setPricePerKwh(connector.getPricePerKw());
        return session;
    }

    public static ChargingSessionResponse toResponse(ChargingSession session) {
        return new ChargingSessionResponse(
                session.getId(),
                session.getSessionCode(),
                session.getStation().getId(),
                session.getConnector().getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getEnergyConsumedKwh(),
                session.getTotalPrice()
        );
    }
}
