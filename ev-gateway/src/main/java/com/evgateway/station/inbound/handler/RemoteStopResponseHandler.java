package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.RemoteStopResultEvent;
import com.evgateway.messaging.publisher.RemoteStopResultPublisher;
import com.evgateway.websocket.dto.StationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class RemoteStopResponseHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(RemoteStopResponseHandler.class);

    private final RemoteStopResultPublisher remoteStopResultPublisher;

    @Override
    public String getMessageType() {
        return "REMOTE_STOP_RESPONSE";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) {
        if (message.getSessionId() == null
                || !StringUtils.hasText(message.getSessionCode())
                || !StringUtils.hasText(message.getStationIdentity())
                || message.getConnectorNumber() == null
                || !StringUtils.hasText(message.getOcppTransactionId())
                || !StringUtils.hasText(message.getResult())) {
            log.warn("Invalid REMOTE_STOP_RESPONSE payload");
            return;
        }

        if (!"ACCEPTED".equals(message.getResult()) && !"REJECTED".equals(message.getResult())) {
            log.warn("Invalid REMOTE_STOP_RESPONSE result: {}", message.getResult());
            return;
        }

        log.info("REMOTE_STOP_RESPONSE received: sessionId={}, sessionCode={}, station={}, connector={}, result={}",
                message.getSessionId(),
                message.getSessionCode(),
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getResult());

        RemoteStopResultEvent event = new RemoteStopResultEvent(
                message.getSessionId(),
                message.getSessionCode(),
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getOcppTransactionId(),
                message.getResult(),
                message.getReason(),
                OffsetDateTime.now()
        );

        remoteStopResultPublisher.publishRemoteStopResponse(event);

        log.info("REMOTE_STOP_RESULT published for station {} and session {}",
                message.getStationIdentity(),
                message.getSessionId());
    }
}
