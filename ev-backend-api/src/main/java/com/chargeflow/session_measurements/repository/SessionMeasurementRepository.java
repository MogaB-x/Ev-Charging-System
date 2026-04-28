package com.chargeflow.session_measurements.repository;

import com.chargeflow.session_measurements.entity.SessionMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SessionMeasurementRepository extends JpaRepository<SessionMeasurement, Long> {
    List<SessionMeasurement> findByChargingSessionIdOrderByRecordedAtAscIdAsc(Long chargingSessionId);

    Optional<SessionMeasurement> findTopByChargingSessionIdAndMeterValueWhIsNotNullOrderByRecordedAtDescIdDesc(Long chargingSessionId);

    @Query("""
            select avg(sm.powerKw)
            from SessionMeasurement sm
            where sm.chargingSession.id = :chargingSessionId
            """)
    BigDecimal findAveragePowerKwByChargingSessionId(@Param("chargingSessionId") Long chargingSessionId);
}
