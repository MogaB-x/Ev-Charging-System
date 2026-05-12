package com.chargeflow.messaging;

import com.chargeflow.messaging.config.RabbitMqConfig;
import com.chargeflow.messaging.contract.command.RemoteStopCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class RemoteStopCommandPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(RemoteStopCommand command) {
        Runnable publishAction = () -> rabbitTemplate.convertAndSend(
                RabbitMqConfig.EV_COMMANDS_EXCHANGE,
                RabbitMqConfig.CHARGING_REMOTE_STOP_ROUTING_KEY,
                command
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishAction.run();
                }
            });
            return;
        }

        publishAction.run();
    }
}
