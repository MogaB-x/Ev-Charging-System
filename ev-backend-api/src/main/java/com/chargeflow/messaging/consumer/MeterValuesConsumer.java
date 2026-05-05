package com.chargeflow.messaging.consumer;

import com.chargeflow.messaging.config.RabbitMqConfig;
import com.chargeflow.messaging.contract.event.MeterValuesReceivedEvent;
import com.chargeflow.session_measurements.service.SessionMeasurementService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeterValuesConsumer {

    private static final Logger log = LoggerFactory.getLogger(MeterValuesConsumer.class);

    private final SessionMeasurementService sessionMeasurementService;

    @RabbitListener(queues = RabbitMqConfig.CORE_METER_VALUES_QUEUE)
    public void consume(MeterValuesReceivedEvent event) {
        log.info("METER_VALUES consumed for sessionId={}, station={}, connector={}",
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber());

        sessionMeasurementService.handleMeterValuesEvent(event);
    }
}

