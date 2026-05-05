package com.chargeflow.messaging.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EV_EVENTS_EXCHANGE = "ev.events";
    public static final String EV_COMMANDS_EXCHANGE = "ev.commands";

    public static final String CORE_STATION_BOOT_QUEUE = "core.station.boot.queue";
    public static final String CORE_STATION_HEARTBEAT_QUEUE = "core.station.heartbeat.queue";
    public static final String CORE_CONNECTOR_STATUS_QUEUE = "core.connector.status.queue";
    public static final String GATEWAY_REMOTE_START_QUEUE = "gateway.remote_start.queue";
    public static final String CORE_REMOTE_START_RESULT_QUEUE = "core.remote_start_result.queue";
    public static final String CORE_TRANSACTION_STARTED_QUEUE = "core.transaction.started.queue";
    public static final String CORE_METER_VALUES_QUEUE = "core.meter.values.queue";
    public static final String CORE_TRANSACTION_STOPPED_QUEUE = "core.transaction.stopped.queue";

    public static final String STATION_BOOT_ROUTING_KEY = "station.boot";
    public static final String STATION_HEARTBEAT_ROUTING_KEY = "station.heartbeat";
    public static final String CONNECTOR_STATUS_ROUTING_KEY = "connector.status";
    public static final String CHARGING_REMOTE_START_ROUTING_KEY = "charging.remote_start";
    public static final String CHARGING_REMOTE_START_RESULT_ROUTING_KEY = "charging.remote_start.result";
    public static final String CHARGING_TRANSACTION_STARTED_ROUTING_KEY = "charging.transaction.started";
    public static final String CHARGING_METER_VALUES_ROUTING_KEY = "charging.meter.values";
    public static final String CHARGING_TRANSACTION_STOPPED_ROUTING_KEY = "charging.transaction.stopped";



    @Bean
    public DirectExchange evEventsExchange() {
        return new DirectExchange(EV_EVENTS_EXCHANGE);
    }

    @Bean
    public DirectExchange evCommandsExchange() {
        return new DirectExchange(EV_COMMANDS_EXCHANGE);
    }

    @Bean
    public Queue coreStationBootQueue() {
        return new Queue(CORE_STATION_BOOT_QUEUE, true);
    }

    @Bean
    public Queue coreStationHeartbeatQueue() {
        return new Queue(CORE_STATION_HEARTBEAT_QUEUE, true);
    }

    @Bean
    public Queue coreConnectorStatusQueue() {return new Queue(CORE_CONNECTOR_STATUS_QUEUE, true);}

    @Bean
    public Queue coreRemoteStartResultQueue() {return new Queue(CORE_REMOTE_START_RESULT_QUEUE, true);}

    @Bean
    public Queue gatewayRemoteStartQueue() {
        return new Queue(GATEWAY_REMOTE_START_QUEUE, true);
    }

    @Bean
    public Queue coreTransactionStartedQueue() { return new Queue(CORE_TRANSACTION_STARTED_QUEUE, true);}

    @Bean
    public Queue coreMeterValuesQueue() {return new Queue(CORE_METER_VALUES_QUEUE, true);}

    @Bean
    public Queue coreTransactionStoppedQueue() { return new Queue(CORE_TRANSACTION_STOPPED_QUEUE, true);}

    @Bean
    public Binding stationBootBinding(Queue coreStationBootQueue, DirectExchange evEventsExchange) {
        return BindingBuilder
                .bind(coreStationBootQueue)
                .to(evEventsExchange)
                .with(STATION_BOOT_ROUTING_KEY);
    }

    @Bean
    public Binding stationHeartbeatBinding(Queue coreStationHeartbeatQueue, DirectExchange evEventsExchange) {
        return BindingBuilder
                .bind(coreStationHeartbeatQueue)
                .to(evEventsExchange)
                .with(STATION_HEARTBEAT_ROUTING_KEY);
    }

    @Bean
    public Binding connectorStatusBinding(Queue coreConnectorStatusQueue, DirectExchange evEventsExchange){
        return BindingBuilder
                .bind(coreConnectorStatusQueue)
                .to(evEventsExchange)
                .with(CONNECTOR_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding remoteStartBinding(Queue gatewayRemoteStartQueue, DirectExchange evCommandsExchange) {
        return BindingBuilder
                .bind(gatewayRemoteStartQueue)
                .to(evCommandsExchange)
                .with(CHARGING_REMOTE_START_ROUTING_KEY);
    }

    @Bean
    public Binding remoteStartResultBinding(Queue coreRemoteStartResultQueue,
                                            DirectExchange evEventsExchange) {
        return BindingBuilder
                .bind(coreRemoteStartResultQueue)
                .to(evEventsExchange)
                .with(CHARGING_REMOTE_START_RESULT_ROUTING_KEY);
    }

    @Bean
    public Binding chargingTransactionStartedBinding(Queue coreTransactionStartedQueue,
                                              DirectExchange evEventsExchange){
        return BindingBuilder
                .bind(coreTransactionStartedQueue)
                .to(evEventsExchange)
                .with(CHARGING_TRANSACTION_STARTED_ROUTING_KEY);

    }

    @Bean
    public Binding meterValuesBinding(Queue coreMeterValuesQueue, DirectExchange evEventsExchange) {
        return BindingBuilder
                .bind(coreMeterValuesQueue)
                .to(evEventsExchange)
                .with(CHARGING_METER_VALUES_ROUTING_KEY);
    }

    @Bean
    public Binding transactionStoppedBinding(Queue coreTransactionStoppedQueue, DirectExchange evEventsExchange) {
        return BindingBuilder
                .bind(coreTransactionStoppedQueue)
                .to(evEventsExchange)
                .with(CHARGING_TRANSACTION_STOPPED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jacksonJsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonJsonMessageConverter);
        return factory;
    }
}
