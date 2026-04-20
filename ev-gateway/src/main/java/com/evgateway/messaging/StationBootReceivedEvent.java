package com.evgateway.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class StationBootReceivedEvent {
    private String stationIdentity;
    private String model;
    private String firmwareVersion;
    private Instant receivedAt;
}
