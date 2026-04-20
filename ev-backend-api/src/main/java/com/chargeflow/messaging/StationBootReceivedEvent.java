package com.chargeflow.messaging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@Setter
@Getter
public class StationBootReceivedEvent {
    private String stationIdentity;
    private String model;
    private String firmwareVersion;
    private Instant receivedAt;
}
