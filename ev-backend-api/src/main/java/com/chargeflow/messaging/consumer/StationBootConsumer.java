package com.chargeflow.messaging.consumer;

import com.chargeflow.common.config.RabbitMqConfig;
import com.chargeflow.messaging.StationBootReceivedEvent;
import com.chargeflow.station.service.StationServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StationBootConsumer {
    private final StationServiceImpl stationBootService;

    public StationBootConsumer(StationServiceImpl stationBootService) {
        this.stationBootService = stationBootService;
    }

    @RabbitListener(queues = RabbitMqConfig.CORE_STATION_BOOT_QUEUE)
    public void consume(StationBootReceivedEvent event) {
        stationBootService.handleBootNotification(event);
    }
}
