package com.chargeflow.messaging.contract.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConnectorStatusReceivedEvent {
    private String stationIdentity;
    private Integer connectorNumber;
    private String status;
    private OffsetDateTime receivedAt;
}
