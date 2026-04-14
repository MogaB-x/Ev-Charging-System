package com.chargeflow.connector.controller;

import com.chargeflow.connector.dto.ConnectorResponse;
import com.chargeflow.connector.service.ConnectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/station/{stationId}/connectors")
@RequiredArgsConstructor
public class ConnectorController {
    private final ConnectorService connectorService;

    @GetMapping
    public List<ConnectorResponse> getConnectorsByStation(@PathVariable Long stationId) {
        return connectorService.getConnectorsByStation(stationId);
    }

    @GetMapping("/available")
    public List<ConnectorResponse> getAvailableConnectorsByStation(@PathVariable Long stationId) {
        return connectorService.getAvailableConnectorsByStation(stationId);
    }
}
