package com.evgateway.messaging.publisher;

import com.evgateway.config.RabbitMqConfig;
import com.evgateway.messaging.RemoteStartResultEvent;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@AllArgsConstructor
public class RemoteStartResultPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishRemoteStartResponse(RemoteStartResultEvent event){
        rabbitTemplate.convertAndSend(
                RabbitMqConfig.EV_EVENTS_EXCHANGE,
                RabbitMqConfig.CHARGING_REMOTE_START_RESULT_ROUTING_KEY,
                event
        );
    }

    public void publishAccepted(Long sessionId,
                                String sessionCode,
                                String stationIdentity,
                                Integer connectorNumber) {
        publishResult(sessionId, sessionCode, stationIdentity, connectorNumber, "ACCEPTED", null);
    }

    public void publishRejected(Long sessionId,
                                String sessionCode,
                                String stationIdentity,
                                Integer connectorNumber,
                                String reason) {
        publishResult(sessionId, sessionCode, stationIdentity, connectorNumber, "REJECTED", reason);
    }

    private void publishResult(Long sessionId,
                               String sessionCode,
                               String stationIdentity,
                               Integer connectorNumber,
                               String result,
                               String reason) {
        RemoteStartResultEvent event = new RemoteStartResultEvent(
                sessionId,
                sessionCode,
                stationIdentity,
                connectorNumber,
                result,
                reason,
                OffsetDateTime.now()
        );
        publishRemoteStartResponse(event);
    }
}
