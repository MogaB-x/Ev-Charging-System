package com.evgateway.messaging.consumer;

import com.evgateway.config.RabbitMqConfig;
import com.evgateway.messaging.RemoteStartCommand;
import com.evgateway.messaging.publisher.RemoteStartResultPublisher;
import com.evgateway.service.StationRegistryService;
import com.evgateway.websocket.dto.RemoteStartWsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RemoteStartCommandConsumer {

    private final StationRegistryService stationRegistryService;
    private final ObjectMapper objectMapper;
    private final RemoteStartResultPublisher remoteStartResultPublisher;

    @RabbitListener(queues = RabbitMqConfig.GATEWAY_REMOTE_START_QUEUE)
    public void consume(RemoteStartCommand command) {
        WebSocketSession session = stationRegistryService.getSession(command.getStationIdentity());

        if (session == null || !session.isOpen()) {
            remoteStartResultPublisher.publishRejected(
                    command.getSessionId(),
                    command.getSessionCode(),
                    command.getStationIdentity(),
                    command.getConnectorNumber(),
                    "STATION_NOT_CONNECTED"
            );
            return;
        }

        try {
            RemoteStartWsCommand wsCommand = new RemoteStartWsCommand(
                    "REMOTE_START_COMMAND",
                    command.getSessionId(),
                    command.getSessionCode(),
                    command.getStationIdentity(),
                    command.getConnectorNumber()
            );

            String json = objectMapper.writeValueAsString(wsCommand);
            session.sendMessage(new TextMessage(json));

        } catch (Exception ex) {
            remoteStartResultPublisher.publishRejected(
                    command.getSessionId(),
                    command.getSessionCode(),
                    command.getStationIdentity(),
                    command.getConnectorNumber(),
                    "WS_SEND_FAILED"
            );
        }
    }
}
