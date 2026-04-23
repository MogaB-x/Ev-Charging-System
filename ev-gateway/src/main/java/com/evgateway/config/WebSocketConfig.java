package com.evgateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.evgateway.websocket.handler.ChargePointWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChargePointWebSocketHandler chargePointWebSocketHandler;

    public WebSocketConfig(ChargePointWebSocketHandler chargePointWebSocketHandler){
        this.chargePointWebSocketHandler = chargePointWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry){
        registry.addHandler(chargePointWebSocketHandler, "/ws/charge-point")
                .setAllowedOrigins("*");
    }
}
