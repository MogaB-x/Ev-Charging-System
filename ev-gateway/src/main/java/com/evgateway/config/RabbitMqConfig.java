package com.evgateway.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    public static final String EV_EVENTS_EXCHANGE = "ev.events";
    public static final String STATION_BOOT_ROUTING_KEY = "station.boot";

    @Bean
    public DirectExchange evEventsExchange(){
        return new DirectExchange(EV_EVENTS_EXCHANGE);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
