package com.evgateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StationMessageProcessorTest {

    private StationRegistryService stationRegistryService;
    private com.evgateway.messaging.publisher.RemoteStartResultPublisher remoteStartResultPublisher;
    private com.evgateway.messaging.publisher.StationEventPublisher stationEventPublisher;
    private WebSocketSession session;
    private StationMessageProcessor stationMessageProcessor;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        stationRegistryService = mock(StationRegistryService.class);
        remoteStartResultPublisher = mock(com.evgateway.messaging.publisher.RemoteStartResultPublisher.class);
        stationEventPublisher = mock(com.evgateway.messaging.publisher.StationEventPublisher.class);
        session = mock(WebSocketSession.class);
        stationMessageProcessor = new StationMessageProcessor(
                objectMapper,
                stationRegistryService,
                remoteStartResultPublisher,
                stationEventPublisher
        );
    }

    @Test
    void processHeartbeatPublishesEventAndRespondsForKnownStation() throws Exception {
        when(stationRegistryService.updateHeartbeat("station-1")).thenReturn(true);

        stationMessageProcessor.process(session, "{\"type\":\"HEARTBEAT\",\"stationIdentity\":\"station-1\"}");

        verify(stationEventPublisher).publishHeartbeatNotification(any());

        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(messageCaptor.capture());

        String payload = messageCaptor.getValue().getPayload();
        assertTrue(payload.contains("HEARTBEAT_RESPONSE"));
        assertTrue(payload.contains("currentTime"));
    }

    @Test
    void processHeartbeatDoesNotPublishForUnknownStation() throws Exception {
        when(stationRegistryService.updateHeartbeat("station-unknown")).thenReturn(false);

        stationMessageProcessor.process(session, "{\"type\":\"HEARTBEAT\",\"stationIdentity\":\"station-unknown\"}");

        verify(stationEventPublisher, never()).publishHeartbeatNotification(any());
        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void processHeartbeatDoesNotRespondWhenPublishFails() throws Exception {
        when(stationRegistryService.updateHeartbeat("station-1")).thenReturn(true);
        doThrow(new RuntimeException("rabbit down"))
                .when(stationEventPublisher)
                .publishHeartbeatNotification(any());

        stationMessageProcessor.process(session, "{\"type\":\"HEARTBEAT\",\"stationIdentity\":\"station-1\"}");

        verify(stationEventPublisher).publishHeartbeatNotification(any());
        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void processRemoteStartResponsePublishesRemoteStartResult() throws Exception {
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

        verify(remoteStartResultPublisher).publishRemoteStartResponse(any());
        verify(session, never()).sendMessage(any(TextMessage.class));
    }
}

