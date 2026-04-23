package com.chargeflow.connector.service;

import com.chargeflow.connector.dto.ConnectorResponse;
import com.chargeflow.connector.dto.CreateConnectorRequest;
import com.chargeflow.connector.dto.UpdateConnectorRequest;
import com.chargeflow.messaging.contract.event.ConnectorStatusReceivedEvent;

import java.util.List;

public interface ConnectorService {
    List<ConnectorResponse> getConnectorsByStation(Long stationId);

    List<ConnectorResponse> getAvailableConnectorsByStation(Long stationId);

    ConnectorResponse getConnectorById(Long connectorId);

    ConnectorResponse createConnector(CreateConnectorRequest request);

    ConnectorResponse updateConnector(Long connectorId, UpdateConnectorRequest request);

    void handleConnectorStatus(ConnectorStatusReceivedEvent event);
}
