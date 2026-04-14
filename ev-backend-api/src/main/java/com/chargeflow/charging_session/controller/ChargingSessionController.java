package com.chargeflow.charging_session.controller;

import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.charging_session.dto.StartSessionRequest;
import com.chargeflow.charging_session.service.ChargingSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/charging-sessions")
@RequiredArgsConstructor
public class ChargingSessionController {

    private final ChargingSessionService service;

    @PostMapping("/start")
    public ChargingSessionResponse startSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StartSessionRequest request
    ) {
        return service.startSession(userDetails.getUsername(), request);
    }

    @PostMapping("/{sessionId}/stop")
    public ChargingSessionResponse stopSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long sessionId
    ) {
        return service.stopSession(userDetails.getUsername(), sessionId);
    }

    @GetMapping("/my")
    public List<ChargingSessionResponse> getMySessions(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return service.getMySessions(userDetails.getUsername());
    }

    @GetMapping("/{sessionId}")
    public ChargingSessionResponse getSessionById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long sessionId
    ) {
        return service.getSessionById(userDetails.getUsername(), sessionId);
    }
}
