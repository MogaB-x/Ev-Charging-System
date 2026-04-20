package com.evgateway.messaging.publisher;

import com.evgateway.config.RabbitMqConfig;
import com.evgateway.messaging.StationBootReceivedEvent;
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
}
