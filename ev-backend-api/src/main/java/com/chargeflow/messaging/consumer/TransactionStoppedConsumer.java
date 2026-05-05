package com.chargeflow.messaging.consumer;

import com.chargeflow.charging_session.service.ChargingSessionServiceImpl;
import com.chargeflow.messaging.config.RabbitMqConfig;
import com.chargeflow.messaging.contract.event.TransactionStoppedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionStoppedConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransactionStoppedConsumer.class);

    private final ChargingSessionServiceImpl chargingSessionService;

    @RabbitListener(queues = RabbitMqConfig.CORE_TRANSACTION_STOPPED_QUEUE)
    public void consume(TransactionStoppedEvent event) {
        log.info("STOP_TRANSACTION consumed for sessionId={}", event.getSessionId());
        chargingSessionService.handleTransactionStoppedEvent(event);
    }
}
