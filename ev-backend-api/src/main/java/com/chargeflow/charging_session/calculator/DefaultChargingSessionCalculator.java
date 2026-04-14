package com.chargeflow.charging_session.calculator;

import com.chargeflow.charging_session.entity.ChargingSession;
import com.chargeflow.common.exception.ConflictException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DefaultChargingSessionCalculator implements ChargingSessionCalculator{
    @Override
    public void updateLiveAggregates(
            ChargingSession session,
            Long meterValueWh,
            BigDecimal averagePowerKw
    ) {
        if (meterValueWh != null) {
            if (session.getMeterStartWh() == null) {
                session.setMeterStartWh(meterValueWh);
            }

            session.setMeterStopWh(meterValueWh);

            long consumedWh = session.getMeterStopWh() - session.getMeterStartWh();
            if (consumedWh < 0) {
                throw new ConflictException("Meter stop value cannot be lower than meter start value");
            }

            BigDecimal energyConsumedKwh = BigDecimal.valueOf(consumedWh)
                    .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

            session.setEnergyConsumedKwh(energyConsumedKwh);

            if (session.getPricePerKwh() != null) {
                BigDecimal totalPrice = energyConsumedKwh
                        .multiply(session.getPricePerKwh())
                        .setScale(2, RoundingMode.HALF_UP);

                session.setTotalPrice(totalPrice);
            }
        }

        if (averagePowerKw != null) {
            session.setAveragePowerKw(averagePowerKw.setScale(2, RoundingMode.HALF_UP));
        }
    }

    @Override
    public void recalculateSessionTotals(ChargingSession session) {

        Long meterStartWh = session.getMeterStartWh();

        Long meterStopWh = session.getMeterStopWh();

        if (meterStartWh == null || meterStopWh == null) {
            session.setEnergyConsumedKwh(BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP));
            session.setTotalPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return;
        }

        long consumedWh = meterStopWh - meterStartWh;

        if (consumedWh < 0) {
            throw new ConflictException("Meter stop value cannot be lower than meter start value");
        }

        BigDecimal energyConsumedKwh = BigDecimal.valueOf(consumedWh)
                .divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        session.setEnergyConsumedKwh(energyConsumedKwh);

        if (session.getPricePerKwh() != null) {
            BigDecimal totalPrice = energyConsumedKwh
                    .multiply(session.getPricePerKwh())
                    .setScale(2, RoundingMode.HALF_UP);

            session.setTotalPrice(totalPrice);
        } else {
            session.setTotalPrice(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }
}
