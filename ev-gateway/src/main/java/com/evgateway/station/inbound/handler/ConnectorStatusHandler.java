package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.ConnectorStatusReceivedEvent;
import com.evgateway.messaging.publisher.StationEventPublisher;
import com.evgateway.model.ConnectorStatus;
import com.evgateway.websocket.dto.StationMessage;
import com.evgateway.websocket.dto.StatusNotificationResponse;
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
public class ConnectorStatusHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(ConnectorStatusHandler.class);

    private final ObjectMapper objectMapper;
    private final StationEventPublisher stationEventPublisher;

    @Override
    public String getMessageType() {
        return "CONNECTOR_STATUS";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) throws Exception {
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
