package com.chargeflow.session_mesurements.controller;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.session_mesurements.dto.CreateSessionMeasurementRequest;
import com.chargeflow.session_mesurements.dto.SessionMeasurementResponse;
import com.chargeflow.session_mesurements.dto.UpdateChargingSessionStatusRequest;
import com.chargeflow.session_mesurements.service.SessionMeasurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/charging-sessions")
@RequiredArgsConstructor
public class InternalChargingSessionMController {

    private final SessionMeasurementService sessionMeasurementsService;

    @PostMapping("/{sessionId}/measurements")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionMeasurementResponse createMeasurement(
            @PathVariable Long sessionId,
            @Valid @RequestBody CreateSessionMeasurementRequest request
    ) {
        return sessionMeasurementsService.createMeasurement(sessionId, request);
    }

    @GetMapping("/{sessionId}/measurements")
    public List<SessionMeasurementResponse> getMeasurementsBySessionId(@PathVariable Long sessionId) {
        return sessionMeasurementsService.getMeasurementsBySessionId(sessionId);
    }

    @PatchMapping("/{sessionId}/status")
    public ChargingSessionResponse updateSessionStatus(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateChargingSessionStatusRequest request
    ) {
        return sessionMeasurementsService.updateSessionStatus(sessionId, request.status(), request.stopReason());
    }

}
