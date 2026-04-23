package com.chargeflow.messaging.contract.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StationBootReceivedEvent {
    private String stationIdentity;
    private String model;
    private String firmwareVersion;
    private OffsetDateTime receivedAt;
}
