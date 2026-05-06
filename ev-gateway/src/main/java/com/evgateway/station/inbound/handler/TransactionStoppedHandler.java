package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.TransactionStoppedEvent;
import com.evgateway.messaging.publisher.StationEventPublisher;
import com.evgateway.websocket.dto.StationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class TransactionStoppedHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionStoppedHandler.class);

    private final StationEventPublisher stationEventPublisher;

    @Override
    public String getMessageType() {
        return "STOP_TRANSACTION";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) {
        if (message.getSessionId() == null
                || !StringUtils.hasText(message.getSessionCode())
                || !StringUtils.hasText(message.getStationIdentity())
                || message.getConnectorNumber() == null
                || !StringUtils.hasText(message.getOcppTransactionId())) {
            log.warn("invalid STOP_TRANSACTION payload");
            return;
        }

        log.info("STOP_TRANSACTION received: station={}, connector={}, sessionId={}",
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getSessionId());

        TransactionStoppedEvent event = new TransactionStoppedEvent(
                message.getStationIdentity(),
                message.getSessionId(),
                message.getSessionCode(),
                message.getConnectorNumber(),
                message.getOcppTransactionId(),
                message.getMeterStopWh(),
                message.getStopReason()
        );

        stationEventPublisher.publishTransactionStopped(event);

        log.info("STOP_TRANSACTION event published for sessionId={}", message.getSessionId());
    }
}
