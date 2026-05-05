package com.evgateway.messaging.contract.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MeterValuesReceivedEvent {
    private Long sessionId;
    private String sessionCode;
    private String stationIdentity;
    private Integer connectorNumber;
    private String ocppTransactionId;
    private BigDecimal powerKw;
    private BigDecimal voltageV;
    private BigDecimal currentA;
    private Long meterValueWh;
}
