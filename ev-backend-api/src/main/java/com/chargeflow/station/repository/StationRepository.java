package com.chargeflow.station.repository;

import com.chargeflow.station.entity.Station;
import com.chargeflow.station.entity.StationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByStationCode(String stationCode);

    Optional<Station> findByOcppIdentity(String ocppIdentity);

    List<Station> findByStatus(StationStatus status);

    boolean existsByStationCode(String stationCode);

    boolean existsByOcppIdentity(String ocppIdentity);
}
