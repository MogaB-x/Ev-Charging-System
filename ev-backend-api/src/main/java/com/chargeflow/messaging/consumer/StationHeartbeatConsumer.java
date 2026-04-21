package com.chargeflow.messaging.consumer;

import com.chargeflow.common.config.RabbitMqConfig;
import com.chargeflow.messaging.StationBootReceivedEvent;
import com.chargeflow.messaging.StationHeartbeatReceivedEvent;
import com.chargeflow.station.service.StationServiceImpl;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class StationHeartbeatConsumer {
    private final StationServiceImpl stationHeartbeatService;

    public StationHeartbeatConsumer(StationServiceImpl stationHeartbeatService) {
        this.stationHeartbeatService = stationHeartbeatService;
    }

    @RabbitListener(queues = RabbitMqConfig.CORE_STATION_HEARTBEAT_QUEUE)
    public void consume(StationHeartbeatReceivedEvent event) {
        stationHeartbeatService.handleHeartbeatNotification(event);
    }
}
