package com.evgateway.websocket.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StationMessage {
    private String type;
    private String stationIdentity;
    private String model;
    private String firmwareVersion;
    private Long sessionId;
    private String sessionCode;
    private Integer connectorNumber;
    private String status;
    private String result;
    private String reason;
    private String ocppTransactionId;
    private Long meterStartWh;
    private BigDecimal powerKw;
    private BigDecimal voltageV;
    private BigDecimal currentA;
    private Long meterValueWh;
    private Long meterStopWh;
    private String stopReason;
}
