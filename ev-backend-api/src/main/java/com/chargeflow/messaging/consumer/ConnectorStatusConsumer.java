package com.chargeflow.messaging.consumer;

import com.chargeflow.messaging.config.RabbitMqConfig;
import com.chargeflow.connector.service.ConnectorService;
import com.chargeflow.messaging.contract.event.ConnectorStatusReceivedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ConnectorStatusConsumer {
    private final ConnectorService connectorStatusService;

    public ConnectorStatusConsumer(ConnectorService connectorStatusService) {
        this.connectorStatusService = connectorStatusService;
    }

    @RabbitListener(queues = RabbitMqConfig.CORE_CONNECTOR_STATUS_QUEUE)
    public void consume(ConnectorStatusReceivedEvent event) {
        connectorStatusService.handleConnectorStatus(event);
    }
}
