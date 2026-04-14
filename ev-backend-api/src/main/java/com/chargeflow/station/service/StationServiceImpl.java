package com.chargeflow.station.service;

import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.common.exception.NotFoundException;
import com.chargeflow.logger.StationAuditLogger;
import com.chargeflow.station.dto.CreateStationRequest;
import com.chargeflow.station.dto.StationResponse;
import com.chargeflow.station.dto.UpdateStationRequest;
import com.chargeflow.station.entity.Station;
import com.chargeflow.station.entity.StationStatus;
import com.chargeflow.station.mapper.StationMapper;
import com.chargeflow.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StationServiceImpl implements StationService {
    private final StationRepository stationRepository;
    private final StationAuditLogger stationAuditLogger;

    @Override
    public List<StationResponse> getActiveStations() {
        try {
            return stationRepository.findByStatus(StationStatus.AVAILABLE)
                    .stream()
                    .map(StationMapper::toResponse)
                    .toList();
        } catch (Exception ex) {
            stationAuditLogger.unexpectedError("getActiveStations", "AVAILABLE", ex);
            throw ex;
        }
    }

    @Override
    public StationResponse getStationById(Long stationId) {
        try {
            Station station = stationRepository.findById(stationId)
                    .orElseThrow(() -> {
                        stationAuditLogger.fetchByIdFailure(stationId, "Station not found");
                        return new NotFoundException("Station not found with id: " + stationId);
                    });

            return StationMapper.toResponse(station);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            stationAuditLogger.unexpectedError("getStationById", String.valueOf(stationId), ex);
            throw ex;
        }
    }

    @Override
    public StationResponse getStationByOcppIdentity(String ocppIdentity) {
        try {
            Station station = stationRepository.findByOcppIdentity(ocppIdentity)
                    .orElseThrow(() -> {
                        stationAuditLogger.fetchByOcppIdentityFailure(ocppIdentity, "Station not found");
                        return new NotFoundException("Station not found with ocppIdentity: " + ocppIdentity);
                    });

            return StationMapper.toResponse(station);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            stationAuditLogger.unexpectedError("getStationByOcppIdentity", ocppIdentity, ex);
            throw ex;
        }
    }

    @Override
    public StationResponse createStation(CreateStationRequest request) {
        stationAuditLogger.createAttempt(request.stationCode());
        try {
            if(stationRepository.existsByStationCode(request.stationCode())) {
                stationAuditLogger.createFailure(request.stationCode(), "Station code already exists");
                throw new ConflictException("Station code already exists: " + request.stationCode());
            }

            if(stationRepository.existsByOcppIdentity(request.ocppIdentity())) {
                stationAuditLogger.createFailure(request.stationCode(), "OCPP identity already exists");
                throw new ConflictException("OCPP identity already exists: " + request.ocppIdentity());
            }

            Station station = StationMapper.toEntity(request);

            Station saved = stationRepository.save(station);
            stationAuditLogger.createSuccess(saved.getId(), saved.getStationCode());
            return StationMapper.toResponse(saved);
        } catch (ConflictException ex) {
            throw ex;
        } catch (Exception ex) {
            stationAuditLogger.unexpectedError("createStation", request.stationCode(), ex);
            throw ex;
        }
    }

    @Override
    public StationResponse updateStation(Long stationId, UpdateStationRequest request) {
        stationAuditLogger.updateAttempt(stationId);
        try {
            Station station = stationRepository.findById(stationId)
                    .orElseThrow(() -> {
                        stationAuditLogger.updateFailure(stationId, "Station not found");
                        return new NotFoundException(
                                "Station with id " + stationId + " was not found"
                        );
                    });

            StationMapper.updateEntity(station, request);

            Station updated = stationRepository.save(station);
            stationAuditLogger.updateSuccess(updated.getId(), updated.getStationCode());
            return StationMapper.toResponse(updated);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            stationAuditLogger.unexpectedError("updateStation", String.valueOf(stationId), ex);
            throw ex;
        }
    }
}
