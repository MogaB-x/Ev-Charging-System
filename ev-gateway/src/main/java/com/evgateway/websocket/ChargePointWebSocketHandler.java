package com.evgateway.websocket;

import com.evgateway.model.ConnectedStation;
import com.evgateway.service.StationMessageProcessor;
import com.evgateway.service.StationRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChargePointWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChargePointWebSocketHandler.class);

    private final StationMessageProcessor stationMessageProcessor;
    private final StationRegistryService stationRegistryService;

    public ChargePointWebSocketHandler(StationMessageProcessor stationMessageProcessor,
                                       StationRegistryService stationRegistryService) {
        this.stationMessageProcessor = stationMessageProcessor;
        this.stationRegistryService = stationRegistryService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("station connected");
        log.info("sessionId = {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        stationMessageProcessor.process(session, message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        ConnectedStation station = stationRegistryService.removeBySessionId(session.getId());

        if (station != null) {
            log.info("station disconnected");
            log.info("stationIdentity = {}", station.getStationIdentity());
            log.info("sessionId = {}", session.getId());
        } else {
            log.info("client disconnected");
            log.info("sessionId = {}", session.getId());
        }

        log.info("status = {}", status);
    }
}
