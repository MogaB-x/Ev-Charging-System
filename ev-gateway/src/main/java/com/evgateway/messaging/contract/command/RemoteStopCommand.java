package com.evgateway.messaging.contract.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RemoteStopCommand {
    private Long sessionId;
    private String sessionCode;
    private String stationIdentity;
    private Integer connectorNumber;
    private String ocppTransactionId;
    private OffsetDateTime requestedAt;
}
