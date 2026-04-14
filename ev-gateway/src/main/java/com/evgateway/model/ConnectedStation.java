package com.evgateway.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class ConnectedStation {
    private String stationIdentity;
    private WebSocketSession session;
    private Instant connectedAt;
    private Instant lastSeenAt;
    private String model;
    private String firmwareVersion;

    private Map<Integer, ConnectorStatus> connectorsStatus = new ConcurrentHashMap();

}
