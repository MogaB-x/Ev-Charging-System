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
public class StationHeartbeatReceivedEvent {
    private String stationIdentity;
    private OffsetDateTime receivedAt;
}
