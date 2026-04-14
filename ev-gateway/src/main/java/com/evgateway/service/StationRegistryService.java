package com.evgateway.service;

import com.evgateway.model.ConnectedStation;
import com.evgateway.model.ConnectorStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StationRegistryService {
    private final Map<String, ConnectedStation> stationsByIdentity = new ConcurrentHashMap<>();

    public void registerBoot(String stationIdentity,
                             WebSocketSession session,
                             String model,
                             String firmwareVersion) {

        Instant now = Instant.now();

        ConnectedStation station = stationsByIdentity.getOrDefault(stationIdentity, new ConnectedStation());
        station.setStationIdentity(stationIdentity);
        station.setSession(session);
        station.setModel(model);
        station.setFirmwareVersion(firmwareVersion);

        if (station.getConnectedAt() == null) {
            station.setConnectedAt(now);
        }

        station.setLastSeenAt(now);

        stationsByIdentity.put(stationIdentity, station);
    }

    public boolean updateHeartbeat(String stationIdentity) {
        ConnectedStation station = stationsByIdentity.get(stationIdentity);
        if (station == null) {
            return false;
        }

        station.setLastSeenAt(Instant.now());
        return true;
    }

    public ConnectedStation findByStationIdentity(String stationIdentity) {
        return stationsByIdentity.get(stationIdentity);
    }

    public Collection<ConnectedStation> getAll() {
        return stationsByIdentity.values();
    }

    public void removeByStationIdentity(String stationIdentity) {
        stationsByIdentity.remove(stationIdentity);
    }

    public ConnectedStation findBySessionId(String sessionId) {
        return stationsByIdentity.values()
                .stream()
                .filter(station -> station.getSession() != null)
                .filter(station -> sessionId.equals(station.getSession().getId()))
                .findFirst()
                .orElse(null);
    }

    public ConnectedStation removeBySessionId(String sessionId) {
        ConnectedStation station = findBySessionId(sessionId);
        if (station != null) {
            stationsByIdentity.remove(station.getStationIdentity());
        }
        return station;
    }

    public ConnectorStatus updateConnectorStatus(String stationIdentity,
                                                 int connectorNumber,
                                                 ConnectorStatus newStatus) {

        ConnectedStation station = stationsByIdentity.get(stationIdentity);

        if (station == null) {
            return null;
        }

        ConnectorStatus oldStatus = station.getConnectorsStatus().get(connectorNumber);

        station.getConnectorsStatus().put(connectorNumber, newStatus);
        station.setLastSeenAt(Instant.now());

        return oldStatus;
    }
}
