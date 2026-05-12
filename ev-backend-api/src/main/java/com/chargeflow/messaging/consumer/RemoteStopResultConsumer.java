package com.chargeflow.messaging.consumer;

import com.chargeflow.charging_session.service.ChargingSessionServiceImpl;
import com.chargeflow.messaging.config.RabbitMqConfig;
import com.chargeflow.messaging.contract.event.RemoteStopResultEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoteStopResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(RemoteStopResultConsumer.class);

    private final ChargingSessionServiceImpl chargingSessionService;

    @RabbitListener(queues = RabbitMqConfig.CORE_REMOTE_STOP_RESULT_QUEUE)
    public void consume(RemoteStopResultEvent event) {
        log.info("RemoteStopResultEvent consumed: sessionId={}, result={}, reason={}",
                event.getSessionId(),
                event.getResult(),
                event.getReason());

        chargingSessionService.handleRemoteStopResultEvent(event);
    }
}
