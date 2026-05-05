package com.chargeflow.messaging.consumer;


import com.chargeflow.charging_session.service.ChargingSessionServiceImpl;
import com.chargeflow.messaging.config.RabbitMqConfig;
import com.chargeflow.messaging.contract.event.TransactionStartedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionStartedConsumer {
    private static final Logger log = LoggerFactory.getLogger(TransactionStartedConsumer.class);

    private final ChargingSessionServiceImpl chargingSessionService;

    @RabbitListener(queues = RabbitMqConfig.CORE_TRANSACTION_STARTED_QUEUE)
    public void consume(TransactionStartedEvent event){

        log.info("ChargingTransactionStarted consumed for station {} , connectorId {} and sessionId {}",
                event.getStationIdentity(),
                event.getConnectorNumber(),
                event.getSessionId()
        );

        chargingSessionService.handleTransactionStartedEvent(event);
    }
}
