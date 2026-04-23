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
    private Long sessionId;
    private String sessionCode;
    private Integer connectorNumber;
    private String status;
    private String result;
    private String reason;
}
