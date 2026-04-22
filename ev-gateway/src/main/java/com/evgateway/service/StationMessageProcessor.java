package com.evgateway.service;

import com.evgateway.messaging.ConnectorStatusReceivedEvent;
import com.evgateway.messaging.StationBootReceivedEvent;
import com.evgateway.messaging.StationHeartbeatReceivedEvent;
import com.evgateway.messaging.publisher.StationEventPublisher;
import com.evgateway.websocket.dto.BootNotificationResponse;
import com.evgateway.websocket.dto.HeartbeatResponse;
import com.evgateway.websocket.dto.StationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;
import com.evgateway.model.ConnectorStatus;
import com.evgateway.websocket.dto.StatusNotificationResponse;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
public class StationMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(StationMessageProcessor.class);

    private final ObjectMapper objectMapper;
    private final StationRegistryService stationRegistryService;
    private final StationEventPublisher stationEventPublisher;

    public StationMessageProcessor(StationRegistryService stationRegistryService,
                                   StationEventPublisher stationEventPublisher) {
        this.objectMapper = new ObjectMapper();
        this.stationRegistryService = stationRegistryService;
        this.stationEventPublisher = stationEventPublisher;
    }

    public void process(WebSocketSession session, String payload) throws Exception {
        StationMessage message = objectMapper.readValue(payload, StationMessage.class);

        if (message.getType() == null) {
            log.warn("message received without type");
            return;
        }

        switch (message.getType()) {
            case "BOOT_NOTIFICATION" -> handleBootNotification(session, message);
            case "HEARTBEAT" -> handleHeartbeat(session, message);
            case "STATUS_NOTIFICATION" -> handleStatusNotification(session, message);
            default -> log.warn("unknown message type received: {}", message.getType());
        }
    }

    private void handleBootNotification(WebSocketSession session, StationMessage message) throws Exception {
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

    private void handleHeartbeat(WebSocketSession session, StationMessage message) {
        log.info("heartbeat received");
        log.info("stationIdentity = {}", message.getStationIdentity());

        if (message.getStationIdentity() == null || message.getStationIdentity().isBlank()) {
            log.warn("HEARTBEAT without stationIdentity");
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

    private void handleStatusNotification(WebSocketSession session, StationMessage message) throws Exception {

        log.info("STATUS_NOTIFICATION received: station={}, connector={}, status={}",
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getStatus());

        if (message.getStationIdentity() == null || message.getConnectorNumber() == null || message.getStatus() == null) {
            log.warn("invalid STATUS_NOTIFICATION payload");
            return;
        }

        try {
            ConnectorStatus.valueOf(message.getStatus());
        } catch (IllegalArgumentException e) {
            log.warn("unknown connector status: {}", message.getStatus());
            return;
        }

        ConnectorStatusReceivedEvent event = new ConnectorStatusReceivedEvent(
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getStatus(),
                OffsetDateTime.now()
        );
        stationEventPublisher.publishConnectorNotification(event);

        StatusNotificationResponse response = new StatusNotificationResponse(
                "STATUS_NOTIFICATION_RESPONSE",
                "RECEIVED"
        );

        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));

        log.info("connector status published to RabbitMQ");
    }

}
