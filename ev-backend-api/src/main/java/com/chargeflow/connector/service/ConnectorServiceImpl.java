package com.chargeflow.connector.service;

import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.common.exception.NotFoundException;
import com.chargeflow.connector.dto.ConnectorResponse;
import com.chargeflow.connector.dto.CreateConnectorRequest;
import com.chargeflow.connector.dto.UpdateConnectorRequest;
import com.chargeflow.connector.entity.Connector;
import com.chargeflow.connector.entity.ConnectorStatus;
import com.chargeflow.connector.mapper.ConnectorMapper;
import com.chargeflow.connector.repository.ConnectorRepository;
import com.chargeflow.logger.ConnectorAuditLogger;
import com.chargeflow.station.entity.Station;
import com.chargeflow.station.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConnectorServiceImpl implements ConnectorService {

    private final ConnectorRepository connectorRepository;
    private final StationRepository stationRepository;
    private final ConnectorAuditLogger connectorAuditLogger;

    @Override
    public List<ConnectorResponse> getConnectorsByStation(Long stationId) {
        return fetchConnectorsByStation(
                stationId,
                () -> connectorRepository.findByStationId(stationId),
                "getConnectorsByStation"
        );
    }

    @Override
    public List<ConnectorResponse> getAvailableConnectorsByStation(Long stationId) {
        return fetchConnectorsByStation(
                stationId,
                () -> connectorRepository.findByStationIdAndConnectorStatus(stationId, ConnectorStatus.AVAILABLE),
                "getAvailableConnectorsByStation"
        );
    }

    @Override
    public ConnectorResponse getConnectorById(Long connectorId) {
        try {
            Connector connector = getConnectorOrThrow(connectorId);
            return ConnectorMapper.toResponse(connector);
        } catch (NotFoundException ex) {
            connectorAuditLogger.fetchByIdFailure(connectorId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            connectorAuditLogger.unexpectedError("getConnectorById", String.valueOf(connectorId), ex);
            throw ex;
        }
    }

    @Override
    @Transactional
    public ConnectorResponse createConnector(CreateConnectorRequest request) {
        connectorAuditLogger.createAttempt(request.stationId(), request.connectorNumber());

        try {
            Station station = getStationOrThrow(request.stationId());
            validateConnectorNumberUniqueness(station.getId(), request.connectorNumber());

            Connector connector = ConnectorMapper.toEntity(request, station);
            Connector savedConnector = connectorRepository.save(connector);

            connectorAuditLogger.createSuccess(
                    savedConnector.getId(),
                    savedConnector.getStation().getId(),
                    savedConnector.getConnectorNumber()
            );

            return ConnectorMapper.toResponse(savedConnector);
        } catch (NotFoundException | ConflictException ex) {
            connectorAuditLogger.createFailure(request.stationId(), request.connectorNumber(), ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            connectorAuditLogger.unexpectedError(
                    "createConnector",
                    request.stationId() + ":" + request.connectorNumber(),
                    ex
            );
            throw ex;
        }
    }

    @Override
    @Transactional
    public ConnectorResponse updateConnector(Long connectorId, UpdateConnectorRequest request) {
        connectorAuditLogger.updateAttempt(connectorId);

        try {
            Connector connector = getConnectorOrThrow(connectorId);
            validateConnectorNumberChange(connector, request.connectorNumber());

            ConnectorMapper.updateEntity(connector, request);
            Connector updatedConnector = connectorRepository.save(connector);

            connectorAuditLogger.updateSuccess(
                    updatedConnector.getId(),
                    updatedConnector.getStation().getId(),
                    updatedConnector.getConnectorNumber()
            );

            return ConnectorMapper.toResponse(updatedConnector);
        } catch (NotFoundException | ConflictException ex) {
            connectorAuditLogger.updateFailure(connectorId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            connectorAuditLogger.unexpectedError("updateConnector", String.valueOf(connectorId), ex);
            throw ex;
        }
    }

    private Station getStationOrThrow(Long stationId) {
        return stationRepository.findById(stationId)
                .orElseThrow(() -> new NotFoundException("Station with id " + stationId + " was not found"));
    }

    private List<ConnectorResponse> fetchConnectorsByStation(
            Long stationId,
            Supplier<List<Connector>> fetchOperation,
            String action
    ) {
        try {
            getStationOrThrow(stationId);

            return fetchOperation.get()
                    .stream()
                    .map(ConnectorMapper::toResponse)
                    .toList();
        } catch (NotFoundException ex) {
            connectorAuditLogger.fetchByStationFailure(stationId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            connectorAuditLogger.unexpectedError(action, String.valueOf(stationId), ex);
            throw ex;
        }
    }

    private Connector getConnectorOrThrow(Long connectorId) {
        return connectorRepository.findById(connectorId)
                .orElseThrow(() -> new NotFoundException("Connector with id " + connectorId + " was not found"));
    }

    private void validateConnectorNumberChange(Connector connector, Integer connectorNumber) {
        if (!connectorNumber.equals(connector.getConnectorNumber())) {
            validateConnectorNumberUniqueness(connector.getStation().getId(), connectorNumber);
        }
    }

    private void validateConnectorNumberUniqueness(Long stationId, Integer connectorNumber) {
        if (connectorRepository.existsByStationIdAndConnectorNumber(stationId, connectorNumber)) {
            throw new ConflictException(
                    "Connector with number " + connectorNumber + " already exists for station " + stationId
            );
        }
    }
}
