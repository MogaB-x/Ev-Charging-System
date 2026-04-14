package com.chargeflow.connector.repository;

import com.chargeflow.connector.entity.Connector;
import com.chargeflow.connector.entity.ConnectorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConnectorRepository extends JpaRepository<Connector, Long> {
    List<Connector> findByStationId(Long stationId);

    List<Connector> findByStationIdAndConnectorStatus(Long stationId, ConnectorStatus status);

    Optional<Connector> findByStationIdAndConnectorNumber(Long stationId, Integer connectorNumber);

    boolean existsByStationIdAndConnectorNumber(Long stationId, Integer connectorNumber);
}
