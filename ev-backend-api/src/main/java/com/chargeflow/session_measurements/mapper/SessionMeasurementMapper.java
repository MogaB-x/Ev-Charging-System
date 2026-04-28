package com.chargeflow.session_measurements.mapper;

import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.session_measurements.dto.CreateSessionMeasurementRequest;
import com.chargeflow.session_measurements.dto.SessionMeasurementResponse;
import com.chargeflow.session_measurements.entity.SessionMeasurement;

import java.time.OffsetDateTime;

public final class SessionMeasurementMapper {

    private SessionMeasurementMapper() {
    }

    public static SessionMeasurement toEntity(CreateSessionMeasurementRequest request, ChargingSession chargingSession) {
        SessionMeasurement sessionMeasurements = new SessionMeasurement();
        sessionMeasurements.setChargingSession(chargingSession);
        sessionMeasurements.setRecordedAt(OffsetDateTime.now());
        sessionMeasurements.setPowerKw(request.powerKw());
        sessionMeasurements.setVoltageV(request.voltageV());
        sessionMeasurements.setCurrentA(request.currentA());
        sessionMeasurements.setMeterValueWh(request.meterValueWh());
        return sessionMeasurements;
    }

    public static SessionMeasurementResponse toResponse(SessionMeasurement sessionMeasurements) {
        return new SessionMeasurementResponse(
                sessionMeasurements.getId(),
                sessionMeasurements.getChargingSession().getId(),
                sessionMeasurements.getRecordedAt(),
                sessionMeasurements.getPowerKw(),
                sessionMeasurements.getVoltageV(),
                sessionMeasurements.getCurrentA(),
                sessionMeasurements.getMeterValueWh()
        );
    }
}
