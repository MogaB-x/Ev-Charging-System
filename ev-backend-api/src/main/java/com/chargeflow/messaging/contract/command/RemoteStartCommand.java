package com.chargeflow.messaging.contract.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RemoteStartCommand{
        Long sessionId;
        String sessionCode;
        String stationIdentity;
        Integer connectorNumber;
        OffsetDateTime requestedAt;
}
