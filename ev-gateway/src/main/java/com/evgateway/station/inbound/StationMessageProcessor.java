package com.evgateway.station.inbound;

import com.evgateway.messaging.contract.event.ConnectorStatusReceivedEvent;
import com.evgateway.messaging.contract.event.RemoteStartResultEvent;
import com.evgateway.messaging.contract.event.StationBootReceivedEvent;
import com.evgateway.messaging.contract.event.StationHeartbeatReceivedEvent;
import com.evgateway.messaging.publisher.RemoteStartResultPublisher;
import com.evgateway.messaging.publisher.StationEventPublisher;
import com.evgateway.model.ConnectorStatus;
import com.evgateway.station.registry.StationRegistryService;
import com.evgateway.websocket.dto.BootNotificationResponse;
import com.evgateway.websocket.dto.HeartbeatResponse;
import com.evgateway.websocket.dto.StationMessage;
import com.evgateway.websocket.dto.StatusNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class StationMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(StationMessageProcessor.class);

    private final ObjectMapper objectMapper;
    private final StationRegistryService stationRegistryService;
    private final RemoteStartResultPublisher remoteStartResultPublisher;
    private final StationEventPublisher stationEventPublisher;


    public void process(WebSocketSession session, String payload) throws Exception {

        StationMessage message = objectMapper.readValue(payload, StationMessage.class);

        if (message.getType() == null) {
            log.warn("message received without type");
            return;
        }

        switch (message.getType()) {
            case "BOOT_NOTIFICATION" -> handleBootNotification(session, message);
            case "HEARTBEAT" -> handleHeartbeat(session, message);
            case "REMOTE_START_RESPONSE" -> handleRemoteStartResponse(message);
            case "CONNECTOR_STATUS" -> handleStatusNotification(session, message);
            default -> log.warn("unknown message type received: {}", message.getType());
        }
    }

    private void handleRemoteStartResponse(StationMessage message) {
        if (message.getSessionId() == null
                || message.getSessionCode() == null
                || message.getStationIdentity() == null
                || message.getConnectorNumber() == null
                || message.getResult() == null) {
            log.warn("Invalid REMOTE_START_RESPONSE payload");
            return;
        }
        if (!"ACCEPTED".equals(message.getResult()) && !"REJECTED".equals(message.getResult())) {
            log.warn("Invalid REMOTE_START_RESPONSE result: {}", message.getResult());
            return;
        }

        log.info("REMOTE_START_RESPONSE received: sessionId={}, sessionCode={}, station={}, connector={}, result={}",
                message.getSessionId(),
                message.getSessionCode(),
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getResult());

        RemoteStartResultEvent event = new RemoteStartResultEvent(
                message.getSessionId(),
                message.getSessionCode(),
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getResult(),
                message.getReason(),
                OffsetDateTime.now()
        );

        remoteStartResultPublisher.publishRemoteStartResponse(event);

        log.info("REMOTE_START_RESULT sent to station {} for session {}",
                message.getStationIdentity(),
                message.getSessionId());
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
