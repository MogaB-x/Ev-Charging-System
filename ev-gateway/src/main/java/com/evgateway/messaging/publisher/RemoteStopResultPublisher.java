package com.evgateway.messaging.publisher;

import com.evgateway.messaging.config.RabbitMqConfig;
import com.evgateway.messaging.contract.event.RemoteStopResultEvent;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@AllArgsConstructor
public class RemoteStopResultPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishRemoteStopResponse(RemoteStopResultEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EV_EVENTS_EXCHANGE,
                RabbitMqConfig.CHARGING_REMOTE_STOP_RESULT_ROUTING_KEY,
                event
        );
    }

    public void publishRejected(Long sessionId,
                                String sessionCode,
                                String stationIdentity,
                                Integer connectorNumber,
                                String ocppTransactionId,
                                String reason) {
        publishResult(sessionId, sessionCode, stationIdentity, connectorNumber, ocppTransactionId, "REJECTED", reason);
    }

    private void publishResult(Long sessionId,
                               String sessionCode,
                               String stationIdentity,
                               Integer connectorNumber,
                               String ocppTransactionId,
                               String result,
                               String reason) {
        RemoteStopResultEvent event = new RemoteStopResultEvent(
                sessionId,
                sessionCode,
                stationIdentity,
                connectorNumber,
                ocppTransactionId,
                result,
                reason,
                OffsetDateTime.now()
        );
        publishRemoteStopResponse(event);
    }
}
