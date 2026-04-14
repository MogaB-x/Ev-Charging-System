package com.chargeflow.session_mesurements.service;

import com.chargeflow.charging_session.calculator.ChargingSessionCalculator;
import com.chargeflow.charging_session.dto.ChargingSessionResponse;
import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.charging_session.repository.ChargingSessionRepository;
import com.chargeflow.charging_session.service.ChargingSessionService;
import com.chargeflow.common.exception.ConflictException;
import com.chargeflow.common.exception.NotFoundException;
import com.chargeflow.session_mesurements.dto.CreateSessionMeasurementRequest;
import com.chargeflow.session_mesurements.dto.SessionMeasurementResponse;
import com.chargeflow.session_mesurements.entity.SessionMeasurement;
import com.chargeflow.session_mesurements.mapper.SessionMeasurementMapper;
import com.chargeflow.session_mesurements.repository.SessionMeasurementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SessionMeasurementServiceImpl implements SessionMeasurementService {

    private final SessionMeasurementRepository measurementRepository;
    private final ChargingSessionRepository chargingSessionRepository;
    private final ChargingSessionService chargingSessionService;
    private final ChargingSessionCalculator chargingSessionCalculator;

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

    @Override
    public ChargingSessionResponse updateSessionStatus(Long sessionId, ChargingStatus status, String stopReason) {
        //verificare tranzitie !
        return chargingSessionService.finalizeSessionInternal(sessionId, status, stopReason);
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

}
