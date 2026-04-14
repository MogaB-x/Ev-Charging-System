package com.chargeflow.session_mesurements.service;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.session_mesurements.dto.CreateSessionMeasurementRequest;
import com.chargeflow.session_mesurements.dto.SessionMeasurementResponse;

import java.util.List;

public interface SessionMeasurementService {
    SessionMeasurementResponse createMeasurement(Long sessionId, CreateSessionMeasurementRequest request);

    List<SessionMeasurementResponse> getMeasurementsBySessionId(Long chargingSessionId);

    ChargingSessionResponse updateSessionStatus(Long sessionId, ChargingStatus status, String stopReason);
}
