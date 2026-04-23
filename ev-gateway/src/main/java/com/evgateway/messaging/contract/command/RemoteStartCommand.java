package com.evgateway.messaging.contract.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RemoteStartCommand {
    Long sessionId;
    String sessionCode;
    String stationIdentity;
    Integer connectorNumber;
    OffsetDateTime requestedAt;
}
