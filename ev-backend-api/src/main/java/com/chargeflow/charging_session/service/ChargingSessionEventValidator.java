package com.chargeflow.charging_session.service;

import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.charging_session.entity.ChargingStatus;
import com.chargeflow.messaging.contract.event.MeterValuesReceivedEvent;
import com.chargeflow.messaging.contract.event.RemoteStartResultEvent;
import com.chargeflow.messaging.contract.event.RemoteStopResultEvent;
import com.chargeflow.messaging.contract.event.TransactionStartedEvent;
import com.chargeflow.messaging.contract.event.TransactionStoppedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

@Component
public class ChargingSessionEventValidator {

    public Optional<String> validateTransactionStarted(ChargingSession session, TransactionStartedEvent event) {
        if (session.getStatus() != ChargingStatus.PENDING) {
            return Optional.of("Session is no longer pending");
        }

        Optional<String> ownershipValidation = validateStationAndConnectorOwnership(
                session,
                event.getStationIdentity(),
                event.getConnectorNumber()
        );
        if (ownershipValidation.isPresent()) {
            return ownershipValidation;
        }

        if (!StringUtils.hasText(event.getOcppTransactionId())) {
            return Optional.of("Missing OCPP transaction id");
        }

        return Optional.empty();
    }

    public Optional<String> validateTransactionStopped(ChargingSession session, TransactionStoppedEvent event) {
        if (session.getStatus() != ChargingStatus.IN_PROGRESS) {
            return Optional.of("Charging session is not in progress");
        }

        Optional<String> ownershipValidation = validateStationAndConnectorOwnership(
                session,
                event.getStationIdentity(),
                event.getConnectorNumber()
        );
        if (ownershipValidation.isPresent()) {
            return ownershipValidation;
        }

        if (!StringUtils.hasText(event.getOcppTransactionId())) {
            return Optional.of("Missing OCPP transaction id");
        }

        if (!Objects.equals(event.getOcppTransactionId(), session.getOcppTransactionId())) {
            return Optional.of("OCPP transaction id does not match the charging session");
        }

        return Optional.empty();
    }

    public Optional<String> validateMeterValues(ChargingSession session, MeterValuesReceivedEvent event) {
        if (session.getStatus() != ChargingStatus.IN_PROGRESS) {
            return Optional.of("Charging session is not in progress");
        }

        Optional<String> ownershipValidation = validateStationAndConnectorOwnership(
                session,
                event.getStationIdentity(),
                event.getConnectorNumber()
        );
        if (ownershipValidation.isPresent()) {
            return ownershipValidation;
        }

        if (!StringUtils.hasText(event.getOcppTransactionId())) {
            return Optional.of("Missing OCPP transaction id");
        }

        if (!Objects.equals(event.getOcppTransactionId(), session.getOcppTransactionId())) {
            return Optional.of("OCPP transaction id does not match the charging session");
        }

        return Optional.empty();
    }

    public Optional<String> validateRemoteStartResult(ChargingSession session, RemoteStartResultEvent event) {
        if (session.getStatus() != ChargingStatus.PENDING) {
            return Optional.of("Session is no longer pending");
        }

        Optional<String> ownershipValidation = validateStationAndConnectorOwnership(
                session,
                event.getStationIdentity(),
                event.getConnectorNumber()
        );
        if (ownershipValidation.isPresent()) {
            return ownershipValidation;
        }

        if (!StringUtils.hasText(event.getResult())) {
            return Optional.of("Missing remote start result");
        }

        if (!"ACCEPTED".equals(event.getResult()) && !"REJECTED".equals(event.getResult())) {
            return Optional.of("Invalid remote start result");
        }

        return Optional.empty();
    }

    public Optional<String> validateRemoteStopResult(ChargingSession session, RemoteStopResultEvent event) {
        if (session.getStatus() != ChargingStatus.IN_PROGRESS) {
            return Optional.of("Charging session is not in progress");
        }

        Optional<String> ownershipValidation = validateStationAndConnectorOwnership(
                session,
                event.getStationIdentity(),
                event.getConnectorNumber()
        );
        if (ownershipValidation.isPresent()) {
            return ownershipValidation;
        }

        if (!StringUtils.hasText(event.getOcppTransactionId())) {
            return Optional.of("Missing OCPP transaction id");
        }

        if (!Objects.equals(event.getOcppTransactionId(), session.getOcppTransactionId())) {
            return Optional.of("OCPP transaction id does not match the charging session");
        }

        if (!StringUtils.hasText(event.getResult())) {
            return Optional.of("Missing remote stop result");
        }

        if (!"ACCEPTED".equals(event.getResult()) && !"REJECTED".equals(event.getResult())) {
            return Optional.of("Invalid remote stop result");
        }

        return Optional.empty();
    }

    private Optional<String> validateStationAndConnectorOwnership(
            ChargingSession session,
            String stationIdentity,
            Integer connectorNumber
    ) {
        if (!Objects.equals(stationIdentity, session.getStation().getOcppIdentity())) {
            return Optional.of("Station identity does not match the charging session");
        }

        if (!Objects.equals(connectorNumber, session.getConnector().getConnectorNumber())) {
            return Optional.of("Connector number does not match the charging session");
        }

        return Optional.empty();
    }
}
