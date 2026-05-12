package com.evgateway.messaging.consumer;

import com.evgateway.messaging.config.RabbitMqConfig;
import com.evgateway.messaging.contract.command.RemoteStopCommand;
import com.evgateway.messaging.publisher.RemoteStopResultPublisher;
import com.evgateway.station.registry.StationRegistryService;
import com.evgateway.websocket.dto.RemoteStopWsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class RemoteStopCommandConsumer {

    private final StationRegistryService stationRegistryService;
    private final ObjectMapper objectMapper;
    private final RemoteStopResultPublisher remoteStopResultPublisher;

    @RabbitListener(queues = RabbitMqConfig.GATEWAY_REMOTE_STOP_QUEUE)
    public void consume(RemoteStopCommand command) {
        WebSocketSession session = stationRegistryService.getSession(command.getStationIdentity());

        if (session == null || !session.isOpen()) {
            remoteStopResultPublisher.publishRejected(
                    command.getSessionId(),
                    command.getSessionCode(),
                    command.getStationIdentity(),
                    command.getConnectorNumber(),
                    command.getOcppTransactionId(),
                    "STATION_NOT_CONNECTED"
            );
            return;
        }

        try {
            RemoteStopWsCommand wsCommand = new RemoteStopWsCommand(
                    "REMOTE_STOP_COMMAND",
                    command.getSessionId(),
                    command.getSessionCode(),
                    command.getStationIdentity(),
                    command.getConnectorNumber(),
                    command.getOcppTransactionId()
            );

            String json = objectMapper.writeValueAsString(wsCommand);
            session.sendMessage(new TextMessage(json));

        } catch (Exception ex) {
            remoteStopResultPublisher.publishRejected(
                    command.getSessionId(),
                    command.getSessionCode(),
                    command.getStationIdentity(),
                    command.getConnectorNumber(),
                    command.getOcppTransactionId(),
                    "WS_SEND_FAILED"
            );
        }
    }
}
