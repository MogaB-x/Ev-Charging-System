package com.evgateway.service;

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

@Service
public class StationMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(StationMessageProcessor.class);

    private final ObjectMapper objectMapper;
    private final StationRegistryService stationRegistryService;
    private final StationBackendClient stationBackendClient;

    public StationMessageProcessor(StationRegistryService stationRegistryService, StationBackendClient stationBackendClient) {
        this.objectMapper = new ObjectMapper();
        this.stationRegistryService = stationRegistryService;
        this.stationBackendClient = stationBackendClient;
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
            return;
        }

        boolean exists = stationBackendClient.stationExists(message.getStationIdentity());

        if (!exists) {
            log.warn("Rejecting station: {}", message.getStationIdentity());

            BootNotificationResponse response = new BootNotificationResponse(
                    "BOOT_NOTIFICATION_RESPONSE",
                    "REJECTED"
            );

            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));

            session.close();

            return;
        }

        log.info("Station validated in backend: {}", message.getStationIdentity());

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

        log.info("boot notification response sent");
    }

    private void handleHeartbeat(WebSocketSession session, StationMessage message) throws Exception {
        log.info("heartbeat received");
        log.info("stationIdentity = {}", message.getStationIdentity());

        boolean updated = stationRegistryService.updateHeartbeat(message.getStationIdentity());

        if (!updated) {
            log.warn("heartbeat received for unknown station: {}", message.getStationIdentity());
            return;
        }

        HeartbeatResponse response = new HeartbeatResponse(
                "HEARTBEAT_RESPONSE",
                Instant.now().toString()
        );

        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));

        log.info("heartbeat response sent");
        log.info("response = {}", jsonResponse);
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

        ConnectorStatus newStatus;

        try {
            newStatus = ConnectorStatus.valueOf(message.getStatus());
        } catch (IllegalArgumentException e) {
            log.warn("unknown connector status: {}", message.getStatus());
            return;
        }

        ConnectorStatus oldStatus = stationRegistryService.updateConnectorStatus(
                message.getStationIdentity(),
                message.getConnectorNumber(),
                newStatus
        );

        if (oldStatus == null) {
            log.warn("station not found or first status set: station={}", message.getStationIdentity());
        } else {
            log.info("Connector status updated: station={}, connector={}, from={}, to={}",
                    message.getStationIdentity(),
                    message.getConnectorNumber(),
                    oldStatus,
                    newStatus);
        }

        StatusNotificationResponse response = new StatusNotificationResponse(
                "STATUS_NOTIFICATION_RESPONSE",
                "RECEIVED"
        );

        String jsonResponse = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonResponse));
    }

}
