package com.chargeflow.messaging.consumer;

import com.chargeflow.charging_session.dto.RemoteStartResultEvent;
import com.chargeflow.charging_session.service.ChargingSessionServiceImpl;
import com.chargeflow.common.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RemoteStartResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(RemoteStartResultConsumer.class);

    private final ChargingSessionServiceImpl chargingSessionService;

    @RabbitListener(queues = RabbitMqConfig.CORE_REMOTE_START_RESULT_QUEUE)
    public void consume(RemoteStartResultEvent event) {
        log.info("RemoteStartResultEvent consumed: sessionId={}, result={}, reason={}",
                event.getSessionId(),
                event.getResult(),
                event.getReason());

        chargingSessionService.handleRemoteStartResult(event);
    }
}
