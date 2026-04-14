package com.chargeflow.charging_session.service;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.charging_session.dto.StartSessionRequest;
import com.chargeflow.charging_session.entity.ChargingStatus;

import java.util.List;

public interface ChargingSessionService {
    ChargingSessionResponse startSession(String userEmail, StartSessionRequest request);

    ChargingSessionResponse stopSession(String userEmail, Long sessionId);

    List<ChargingSessionResponse> getMySessions(String userEmail);

    ChargingSessionResponse getSessionById(String userEmail, Long sessionId);

    ChargingSessionResponse finalizeSessionInternal(Long sessionId, ChargingStatus status, String stopReason);
}
