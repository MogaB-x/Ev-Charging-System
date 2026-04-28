package com.chargeflow.charging_session.repository;

import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {
    List<ChargingSession> findByUserEmail(String userEmail);

    List<ChargingSession> findByUserEmailAndStatus(String userEmail, ChargingStatus status);

    Optional<ChargingSession> findByIdAndUserEmail(Long id, String userEmail);

    boolean existsByUserEmailAndStatus(String userEmail, ChargingStatus status);

    boolean existsByUserEmailAndStatusIn(String userEmail, Collection<ChargingStatus> statuses);

    Optional<ChargingSession> findByConnectorIdAndStatus(Long connectorId, ChargingStatus status);

}
