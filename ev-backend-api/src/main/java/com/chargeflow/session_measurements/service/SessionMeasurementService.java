package com.chargeflow.session_measurements.service;

import com.chargeflow.session_measurements.dto.CreateSessionMeasurementRequest;
import com.chargeflow.session_measurements.dto.SessionMeasurementResponse;

import java.util.List;

public interface SessionMeasurementService {
    SessionMeasurementResponse createMeasurement(Long sessionId, CreateSessionMeasurementRequest request);

    List<SessionMeasurementResponse> getMeasurementsBySessionId(Long chargingSessionId);
}
