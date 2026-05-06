package com.evgateway.station.inbound.handler;

import com.evgateway.messaging.contract.event.MeterValuesReceivedEvent;
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
public class MeterValuesHandler implements StationInboundMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(MeterValuesHandler.class);

    private final StationEventPublisher stationEventPublisher;

    @Override
    public String getMessageType() {
        return "METER_VALUES";
    }

    @Override
    public void handle(WebSocketSession session, StationMessage message) {
        if (message.getSessionId() == null
                || !StringUtils.hasText(message.getSessionCode())
                || !StringUtils.hasText(message.getStationIdentity())
                || message.getConnectorNumber() == null
                || !StringUtils.hasText(message.getOcppTransactionId())) {
            log.warn("invalid METER_VALUES payload");
            return;
        }

        log.info("METER_VALUES received: station={}, connector={}, sessionId={}",
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getSessionId());

        MeterValuesReceivedEvent event = new MeterValuesReceivedEvent(
                message.getSessionId(),
                message.getSessionCode(),
                message.getStationIdentity(),
                message.getConnectorNumber(),
                message.getOcppTransactionId(),
                message.getPowerKw(),
                message.getVoltageV(),
                message.getCurrentA(),
                message.getMeterValueWh()
        );

        stationEventPublisher.publishMeterValues(event);

        log.info("METER_VALUES event published for sessionId={}", message.getSessionId());
    }
}
