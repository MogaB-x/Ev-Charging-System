package com.chargeflow.connector.mapper;

import com.chargeflow.connector.dto.ConnectorResponse;
import com.chargeflow.connector.dto.CreateConnectorRequest;
import com.chargeflow.connector.dto.UpdateConnectorRequest;
import com.chargeflow.connector.entity.Connector;
import com.chargeflow.station.entity.Station;

public final class ConnectorMapper {

    private ConnectorMapper(){
    }

    public static ConnectorResponse toResponse(Connector connector){
        return new ConnectorResponse(
                connector.getId(),
                connector.getStation().getId(),
                connector.getConnectorNumber(),
                connector.getConnectorType(),
                connector.getConnectorStatus(),
                connector.getMaxPowerKw(),
                connector.getPricePerKw(),
                connector.getCreatedAt(),
                connector.getUpdatedAt()
        );
    }

    public static Connector toEntity(CreateConnectorRequest request, Station station){
        Connector connector = new Connector();
        connector.setStation(station);
        connector.setConnectorNumber(request.connectorNumber());
        connector.setConnectorType(request.connectorType());
        connector.setConnectorStatus(request.connectorStatus());
        connector.setMaxPowerKw(request.maxPowerKw());
        connector.setPricePerKw(request.pricePerKw());
        return connector;
    }

    public static void updateEntity(Connector connector, UpdateConnectorRequest request) {
        connector.setConnectorNumber(request.connectorNumber());
        connector.setConnectorType(request.connectorType());
        connector.setConnectorStatus(request.connectorStatus());
        connector.setMaxPowerKw(request.maxPowerKw());
        connector.setPricePerKw(request.pricePerKw());
    }
}
