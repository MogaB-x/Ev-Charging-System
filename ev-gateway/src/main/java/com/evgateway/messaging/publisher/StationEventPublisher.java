package com.evgateway.messaging.publisher;

import com.evgateway.config.RabbitMqConfig;
import com.evgateway.messaging.StationBootReceivedEvent;
import com.evgateway.messaging.StationHeartbeatReceivedEvent;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StationEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishBootNotification(StationBootReceivedEvent event){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EV_EVENTS_EXCHANGE,
                RabbitMqConfig.STATION_BOOT_ROUTING_KEY,
                event
        );
    }

    public void publishHeartbeatNotification(StationHeartbeatReceivedEvent event){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EV_EVENTS_EXCHANGE,
                RabbitMqConfig.STATION_HEARTBEAT_ROUTING_KEY,
                event
        );
    }

    public void publishConnectorNotification(Object event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EV_EVENTS_EXCHANGE,
                RabbitMqConfig.CONNECTOR_STATUS_ROUTING_KEY,
                event
        );
    }
}
