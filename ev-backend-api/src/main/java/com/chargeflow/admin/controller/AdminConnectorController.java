package com.chargeflow.admin.controller;

import com.chargeflow.connector.dto.ConnectorResponse;
import com.chargeflow.connector.dto.CreateConnectorRequest;
import com.chargeflow.connector.dto.UpdateConnectorRequest;
import com.chargeflow.connector.service.ConnectorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/connectors")
@RequiredArgsConstructor
public class AdminConnectorController {
    private final ConnectorService connectorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConnectorResponse createConnector(@Valid @RequestBody CreateConnectorRequest request) {
        return connectorService.createConnector(request);
    }

    @PutMapping("/{connectorId}")
    public ConnectorResponse updateConnector(
            @PathVariable Long connectorId,
            @Valid @RequestBody UpdateConnectorRequest request
    ) {
        return connectorService.updateConnector(connectorId, request);
    }
}
