package com.evgateway.station.inbound.handler;

import com.evgateway.websocket.dto.StationMessage;
import org.springframework.web.socket.WebSocketSession;

public interface StationInboundMessageHandler {

    String getMessageType();

    void handle(WebSocketSession session, StationMessage message) throws Exception;
}
