package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.RemoteStartResultEvent;
import com.evgateway.messaging.publisher.RemoteStartResultPublisher;
import com.evgateway.websocket.dto.StationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class RemoteStartResponseHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(RemoteStartResponseHandler.class);

    private final RemoteStartResultPublisher remoteStartResultPublisher;

    @Override
    public String getMessageType() {
        return "REMOTE_START_RESPONSE";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) {
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
}
