package com.evgateway.websocket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StationMessage {
    private String type;
    private String stationIdentity;
    private String model;
    private String firmwareVersion;
    private Integer connectorNumber;
    private String status;
}
