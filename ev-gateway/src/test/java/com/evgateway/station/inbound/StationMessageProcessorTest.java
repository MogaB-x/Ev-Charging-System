package com.evgateway.station.inbound;

import com.evgateway.station.inbound.handler.StationInboundMessageHandler;
import com.evgateway.websocket.dto.StationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StationMessageProcessorTest {

    private WebSocketSession session;
    private StationInboundMessageHandler heartbeatHandler;
    private StationInboundMessageHandler remoteStartResponseHandler;
    private StationMessageProcessor stationMessageProcessor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        session = mock(WebSocketSession.class);

        heartbeatHandler = mock(StationInboundMessageHandler.class);
        when(heartbeatHandler.getMessageType()).thenReturn("HEARTBEAT");

        remoteStartResponseHandler = mock(StationInboundMessageHandler.class);
        when(remoteStartResponseHandler.getMessageType()).thenReturn("REMOTE_START_RESPONSE");

        stationMessageProcessor = new StationMessageProcessor(
                objectMapper,
                List.of(heartbeatHandler, remoteStartResponseHandler)
        );
    }

    @Test
    void processDispatchesHeartbeatToMatchingHandler() throws Exception {
        stationMessageProcessor.process(session, "{\"type\":\"HEARTBEAT\",\"stationIdentity\":\"station-1\"}");

        verify(heartbeatHandler).handle(any(WebSocketSession.class), any(StationMessage.class));
        verify(remoteStartResponseHandler, never()).handle(any(WebSocketSession.class), any(StationMessage.class));
    }

    @Test
    void processDispatchesRemoteStartResponseToMatchingHandler() throws Exception {
        stationMessageProcessor.process(
                session,
                "{" +
                        "\"type\":\"REMOTE_START_RESPONSE\"," +
                        "\"sessionId\":101," +
                        "\"sessionCode\":\"ABC123\"," +
                        "\"stationIdentity\":\"station-1\"," +
                        "\"connectorNumber\":2," +
                        "\"result\":\"ACCEPTED\"" +
                        "}"
        );

        verify(remoteStartResponseHandler).handle(any(WebSocketSession.class), any(StationMessage.class));
        verify(heartbeatHandler, never()).handle(any(WebSocketSession.class), any(StationMessage.class));
    }

    @Test
    void processIgnoresMessagesWithoutType() throws Exception {
        stationMessageProcessor.process(session, "{\"stationIdentity\":\"station-1\"}");

        verify(heartbeatHandler, never()).handle(any(WebSocketSession.class), any(StationMessage.class));
        verify(remoteStartResponseHandler, never()).handle(any(WebSocketSession.class), any(StationMessage.class));
    }

    @Test
    void processIgnoresUnknownMessageType() throws Exception {
        stationMessageProcessor.process(session, "{\"type\":\"UNKNOWN\",\"stationIdentity\":\"station-1\"}");

        verify(heartbeatHandler, never()).handle(any(WebSocketSession.class), any(StationMessage.class));
        verify(remoteStartResponseHandler, never()).handle(any(WebSocketSession.class), any(StationMessage.class));
    }
}
