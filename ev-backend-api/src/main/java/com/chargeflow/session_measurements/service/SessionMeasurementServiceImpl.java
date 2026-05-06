package com.chargeflow.session_measurements.service;

import com.chargeflow.charging_session.calculator.ChargingSessionCalculator;
import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.charging_session.repository.ChargingSessionRepository;
import com.chargeflow.charging_session.service.ChargingSessionEventValidator;
import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.common.exception.NotFoundException;
import com.chargeflow.logger.SessionMeasurementAuditLogger;
import com.chargeflow.messaging.contract.event.MeterValuesReceivedEvent;
import com.chargeflow.session_measurements.dto.CreateSessionMeasurementRequest;
import com.chargeflow.session_measurements.dto.SessionMeasurementResponse;
import com.chargeflow.session_measurements.entity.SessionMeasurement;
import com.chargeflow.session_measurements.mapper.SessionMeasurementMapper;
import com.chargeflow.session_measurements.repository.SessionMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionMeasurementServiceImpl implements SessionMeasurementService {

    private final SessionMeasurementRepository measurementRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final ChargingSessionCalculator chargingSessionCalculator;
    private final SessionMeasurementAuditLogger sessionMeasurementAuditLogger;
    private final ChargingSessionEventValidator chargingSessionEventValidator;

    @Override
    public void handleMeterValuesEvent(MeterValuesReceivedEvent event) {
        sessionMeasurementAuditLogger.meterValuesReceived(
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber(),
                event.getOcppTransactionId()
        );

        if (event.getSessionId() == null) {
            logMeterValuesIgnored(event, "Missing session id");
            return;
        }

        try {
            ChargingSession session = chargingSessionRepository.findById(event.getSessionId())
                    .orElseThrow(() -> new NotFoundException("Charging session was not found"));

            Optional<String> meterValuesValidation =
                    chargingSessionEventValidator.validateMeterValues(session, event);
            if (meterValuesValidation.isPresent()) {
                logMeterValuesIgnored(event, meterValuesValidation.get());
                return;
            }

            saveMeasurementAndUpdateSession(session, event);
            sessionMeasurementAuditLogger.meterValuesProcessed(
                    session.getId(),
                    session.getStation().getOcppIdentity(),
                    session.getConnector().getConnectorNumber(),
                    session.getOcppTransactionId(),
                    event.getMeterValueWh()
            );
        } catch (NotFoundException | ConflictException ex) {
            logMeterValuesIgnored(event, ex.getMessage());
        }catch (Exception ex) {
            sessionMeasurementAuditLogger.unexpectedError(
                    "handleMeterValuesEvent",
                    String.valueOf(event.getSessionId()),
                    ex
            );
            throw ex;
        }
    }


    @Override
    public SessionMeasurementResponse createMeasurement(Long sessionId, CreateSessionMeasurementRequest request) {
        ChargingSession session = chargingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(
                        "Charging session with id " + sessionId + " was not found"
                ));

        if (session.getStatus() != ChargingStatus.IN_PROGRESS) {
            throw new ConflictException("Measurements can only be added to an active session");
        }

        validateMeterValue(session, request.meterValueWh());

        SessionMeasurement measurement = SessionMeasurementMapper.toEntity(request, session);
        SessionMeasurement saved = measurementRepository.save(measurement);

        BigDecimal avgPower = measurementRepository.findAveragePowerKwByChargingSessionId(session.getId());

        chargingSessionCalculator.updateLiveAggregates(
                session,
                request.meterValueWh(),
                avgPower
        );
        chargingSessionRepository.save(session);

        return SessionMeasurementMapper.toResponse(saved);
    }

    @Override
    public List<SessionMeasurementResponse> getMeasurementsBySessionId(Long chargingSessionId) {
        if (!chargingSessionRepository.existsById(chargingSessionId)) {
            throw new NotFoundException("Charging session with id " + chargingSessionId + " was not found");
        }

        return measurementRepository.findByChargingSessionIdOrderByRecordedAtAscIdAsc(chargingSessionId)
                .stream()
                .map(SessionMeasurementMapper::toResponse)
                .toList();
    }

    private void validateMeterValue(ChargingSession session, Long meterValueWh) {
        if (meterValueWh == null) {
            return;
        }

        Long meterStartWh = session.getMeterStartWh();
        if (meterStartWh != null && meterValueWh < meterStartWh) {
            throw new ConflictException("Meter value cannot be lower than meter start value");
        }

        Long sessionMeterStopWh = session.getMeterStopWh();
        if (sessionMeterStopWh != null && meterValueWh < sessionMeterStopWh) {
            throw new ConflictException("Meter value cannot decrease during an active session");
        }

        measurementRepository
                .findTopByChargingSessionIdAndMeterValueWhIsNotNullOrderByRecordedAtDescIdDesc(session.getId())
                .map(SessionMeasurement::getMeterValueWh)
                .filter(lastMeterValueWh -> meterValueWh < lastMeterValueWh)
                .ifPresent(lastMeterValueWh -> {
                    throw new ConflictException("Meter value cannot be lower than the latest recorded measurement");
                });
    }

    private void saveMeasurementAndUpdateSession(
            ChargingSession session,
            MeterValuesReceivedEvent event
    ) {
        validateMeterValue(session, event.getMeterValueWh());

        SessionMeasurement measurement = SessionMeasurementMapper.toEntity(event, session);

        measurementRepository.save(measurement);

        BigDecimal avgPower = measurementRepository.findAveragePowerKwByChargingSessionId(session.getId());

        chargingSessionCalculator.updateLiveAggregates(
                session,
                event.getMeterValueWh(),
                avgPower
        );

        chargingSessionRepository.save(session);
    }

    private void logMeterValuesIgnored(MeterValuesReceivedEvent event, String reason) {
        sessionMeasurementAuditLogger.meterValuesIgnored(
                event.getSessionId(),
                event.getStationIdentity(),
                event.getConnectorNumber(),
                event.getOcppTransactionId(),
                reason
        );
    }


}
