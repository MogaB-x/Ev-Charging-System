package com.chargeflow.charging_session.calculator;

import com.chargeflow.charging_session.entity.ChargingSession;

import java.math.BigDecimal;

public interface ChargingSessionCalculator {
    void updateLiveAggregates(
            ChargingSession session,
            Long meterValueWh,
            BigDecimal averagePowerKw
    );

    void recalculateSessionTotals(ChargingSession session);
}
