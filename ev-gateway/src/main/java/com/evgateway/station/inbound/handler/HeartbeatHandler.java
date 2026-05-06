package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.StationHeartbeatReceivedEvent;
import com.evgateway.messaging.publisher.StationEventPublisher;
import com.evgateway.station.registry.StationRegistryService;
import com.evgateway.websocket.dto.HeartbeatResponse;
import com.evgateway.websocket.dto.StationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class HeartbeatHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(HeartbeatHandler.class);

    private final ObjectMapper objectMapper;
    private final StationRegistryService stationRegistryService;
    private final StationEventPublisher stationEventPublisher;

    @Override
    public String getMessageType() {
        return "HEARTBEAT";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) throws Exception {
        log.info("heartbeat received");
        log.info("stationIdentity = {}", message.getStationIdentity());

        if (message.getStationIdentity() == null || message.getStationIdentity().isBlank()) {
            log.warn("HEARTBEAT without stationIdentity");
            return;
        }

        boolean updated = stationRegistryService.updateHeartbeat(message.getStationIdentity());

        if (!updated) {
            log.warn("heartbeat received for unknown station: {}", message.getStationIdentity());
            return;
        }

        try {
            StationHeartbeatReceivedEvent event = new StationHeartbeatReceivedEvent(
                    message.getStationIdentity(),
                    OffsetDateTime.now()
            );

            stationEventPublisher.publishHeartbeatNotification(event);

            HeartbeatResponse response = new HeartbeatResponse(
                    "HEARTBEAT_RESPONSE",
                    Instant.now().toString()
            );

            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));

            log.info("heartbeat notification published to RabbitMQ and response sent");
            log.info("response = {}", jsonResponse);
        } catch (Exception e) {
            log.error("Failed to publish heartbeat notification to RabbitMQ", e);
        }
    }
}
