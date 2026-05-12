package com.evgateway.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EV_EVENTS_EXCHANGE = "ev.events";
    public static final String EV_COMMANDS_EXCHANGE = "ev.commands";

    public static final String GATEWAY_REMOTE_START_QUEUE = "gateway.remote_start.queue";
    public static final String GATEWAY_REMOTE_STOP_QUEUE = "gateway.remote_stop.queue";

    public static final String STATION_BOOT_ROUTING_KEY = "station.boot";
    public static final String STATION_HEARTBEAT_ROUTING_KEY = "station.heartbeat";
    public static final String CONNECTOR_STATUS_ROUTING_KEY = "connector.status";

    public static final String CHARGING_REMOTE_START_RESULT_ROUTING_KEY = "charging.remote_start.result";
    public static final String CHARGING_REMOTE_STOP_ROUTING_KEY = "charging.remote_stop";
    public static final String CHARGING_REMOTE_STOP_RESULT_ROUTING_KEY = "charging.remote_stop.result";

    public static final String CHARGING_TRANSACTION_STARTED_ROUTING_KEY = "charging.transaction.started";

    public static final String CHARGING_METER_VALUES_ROUTING_KEY = "charging.meter.values";

    public static final String CHARGING_TRANSACTION_STOPPED_ROUTING_KEY = "charging.transaction.stopped";

    @Bean
    public DirectExchange evEventsExchange(){
        return new DirectExchange(EV_EVENTS_EXCHANGE);
    }

    @Bean
    public DirectExchange evCommandsExchange(){
        return new DirectExchange(EV_COMMANDS_EXCHANGE);
    }

    @Bean
    public Queue gatewayRemoteStopQueue() {
        return new Queue(GATEWAY_REMOTE_STOP_QUEUE, true);
    }

    @Bean
    public Binding remoteStopBinding(Queue gatewayRemoteStopQueue, DirectExchange evCommandsExchange) {
        return BindingBuilder
                .bind(gatewayRemoteStopQueue)
                .to(evCommandsExchange)
                .with(CHARGING_REMOTE_STOP_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
