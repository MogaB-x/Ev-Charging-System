package com.evgateway.station.inbound;

import com.evgateway.station.inbound.handler.StationInboundMessageHandler;
import com.evgateway.websocket.dto.StationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StationMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(StationMessageProcessor.class);

    private final ObjectMapper objectMapper;
    private final Map<String, StationInboundMessageHandler> handlersByType;

    public StationMessageProcessor(
            ObjectMapper objectMapper,
            List<StationInboundMessageHandler> handlers
    ) {
        this.objectMapper = objectMapper;
        this.handlersByType = handlers.stream()
                .collect(Collectors.toMap(StationInboundMessageHandler::getMessageType, Function.identity()));
    }

    public void process(WebSocketSession session, String payload) throws Exception {

        StationMessage message = objectMapper.readValue(payload, StationMessage.class);

        if (message.getType() == null) {
            log.warn("message received without type");
            return;
        }

        StationInboundMessageHandler handler = handlersByType.get(message.getType());
        if (handler == null) {
            log.warn("unknown message type received: {}", message.getType());
            return;
        }
        handler.handle(session, message);
    }

}
