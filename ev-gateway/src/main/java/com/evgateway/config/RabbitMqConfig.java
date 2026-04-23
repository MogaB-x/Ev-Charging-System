package com.evgateway.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EV_EVENTS_EXCHANGE = "ev.events";

    public static final String GATEWAY_REMOTE_START_QUEUE = "gateway.remote_start.queue";

    public static final String STATION_BOOT_ROUTING_KEY = "station.boot";
    public static final String STATION_HEARTBEAT_ROUTING_KEY = "station.heartbeat";
    public static final String CONNECTOR_STATUS_ROUTING_KEY = "connector.status";

    public static final String CHARGING_REMOTE_START_RESULT_ROUTING_KEY = "charging.remote_start.result";

    @Bean
    public DirectExchange evEventsExchange(){
        return new DirectExchange(EV_EVENTS_EXCHANGE);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
