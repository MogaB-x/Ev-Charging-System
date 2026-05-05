package com.evgateway.messaging.contract.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TransactionStartedEvent {
    private Long sessionId;
    private String sessionCode;
    private String stationIdentity;
    private Integer connectorNumber;
    private String ocppTransactionId;
    private Long meterStartWh;
}
