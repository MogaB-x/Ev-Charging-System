package com.evgateway.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RemoteStartWsCommand {
    private String type;
    private Long sessionId;
    private String sessionCode;
    private String stationIdentity;
    private Integer connectorNumber;
}

