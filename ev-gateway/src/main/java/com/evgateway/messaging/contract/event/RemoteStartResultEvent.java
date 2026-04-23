package com.evgateway.messaging.contract.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RemoteStartResultEvent {
    private Long sessionId;
    private String sessionCode;
    private String stationIdentity;
    private Integer connectorNumber;
    private String result;
    private String reason;
    private OffsetDateTime receivedAt;
}
