package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.TransactionStartedEvent;
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
public class TransactionStartedHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionStartedHandler.class);

    private final StationEventPublisher stationEventPublisher;

    @Override
    public String getMessageType() {
        return "TRANSACTION_STARTED";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) {
        if (message.getSessionId() == null
                || !StringUtils.hasText(message.getSessionCode())
                || !StringUtils.hasText(message.getStationIdentity())
                || message.getConnectorNumber() == null
                || !StringUtils.hasText(message.getOcppTransactionId())) {
            log.warn("invalid TRANSACTION_STARTED payload");
            return;
        }

        log.info("TRANSACTION_STARTED received: station={}, connector={}, sessionId={}",
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getSessionId());

        TransactionStartedEvent event = new TransactionStartedEvent(
                message.getSessionId(),
                message.getSessionCode(),
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getOcppTransactionId(),
                message.getMeterStartWh()
        );

        stationEventPublisher.publishTransactionStarted(event);

        log.info("TRANSACTION_STARTED event published to RabbitMQ for station {} and sessionId {}",
                message.getStationIdentity(),
                message.getSessionId()
        );
    }
}
