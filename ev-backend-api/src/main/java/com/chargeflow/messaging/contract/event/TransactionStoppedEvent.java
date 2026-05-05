package com.chargeflow.messaging.contract.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionStoppedEvent {
    private String stationIdentity;
    private Long sessionId;
    private String sessionCode;
    private Integer connectorNumber;
    private String ocppTransactionId;
    private Long meterStopWh;
    private String stopReason;
}

