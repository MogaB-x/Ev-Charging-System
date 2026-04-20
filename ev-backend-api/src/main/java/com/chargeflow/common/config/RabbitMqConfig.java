package com.chargeflow.common.config;

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
    public static final String CORE_STATION_BOOT_QUEUE = "core.station.boot.queue";
    public static final String STATION_BOOT_ROUTING_KEY = "station.boot";

    @Bean
    public DirectExchange evEventsExchange() {
        return new DirectExchange(EV_EVENTS_EXCHANGE);
    }

    @Bean
    public Queue coreStationBootQueue() {
        return new Queue(CORE_STATION_BOOT_QUEUE, true);
    }

    @Bean
    public Binding stationBootBinding(Queue coreStationBootQueue, DirectExchange evEventsExchange) {
        return BindingBuilder
                .bind(coreStationBootQueue)
                .to(evEventsExchange)
                .with(STATION_BOOT_ROUTING_KEY);
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
