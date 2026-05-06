package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.StationBootReceivedEvent;
import com.evgateway.messaging.publisher.StationEventPublisher;
import com.evgateway.station.registry.StationRegistryService;
import com.evgateway.websocket.dto.BootNotificationResponse;
import com.evgateway.websocket.dto.StationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class BootNotificationHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(BootNotificationHandler.class);

    private final ObjectMapper objectMapper;
    private final StationRegistryService stationRegistryService;
    private final StationEventPublisher stationEventPublisher;

    @Override
    public String getMessageType() {
        return "BOOT_NOTIFICATION";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) throws Exception {
        log.info("boot notification received");
        log.info("stationIdentity = {}", message.getStationIdentity());

        if (message.getStationIdentity() == null || message.getStationIdentity().isBlank()) {
            log.warn("BOOT without stationIdentity");
            BootNotificationResponse response = new BootNotificationResponse(
                    "BOOT_NOTIFICATION_RESPONSE",
                    "REJECTED"
            );

            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
            return;
        }

        try {
            StationBootReceivedEvent event = new StationBootReceivedEvent(
                    message.getStationIdentity(),
                    message.getModel(),
                    message.getFirmwareVersion(),
                    OffsetDateTime.now()
            );

            stationEventPublisher.publishBootNotification(event);

            stationRegistryService.registerBoot(
                    message.getStationIdentity(),
                    session,
                    message.getModel(),
                    message.getFirmwareVersion()
            );

            BootNotificationResponse response = new BootNotificationResponse(
                    "BOOT_NOTIFICATION_RESPONSE",
                    "ACCEPTED"
            );

            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));

            log.info("boot notification published to RabbitMQ and response sent");

        } catch (Exception e) {
            log.error("Failed to publish boot notification to RabbitMQ", e);

            BootNotificationResponse response = new BootNotificationResponse(
                    "BOOT_NOTIFICATION_RESPONSE",
                    "REJECTED"
            );

            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
        }
    }
}
