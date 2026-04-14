package com.chargeflow.session_mesurements.entity;

import com.chargeflow.charging_session.entity.ChargingSession;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "charging_session_measurements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionMeasurement {

	@Id
	@Setter(AccessLevel.NONE)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "charging_session_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_measurements_charging_session")
	)
	private ChargingSession chargingSession;

	@Column(name = "recorded_at", nullable = false)
	private OffsetDateTime recordedAt;

	@Column(name = "power_kw", nullable = false, precision = 10, scale = 2)
	private BigDecimal powerKw;

	@Column(name = "voltage_v", precision = 10, scale = 2)
	private BigDecimal voltageV;

	@Column(name = "current_a", precision = 10, scale = 2)
	private BigDecimal currentA;

	@Column(name = "meter_value_wh")
	private Long meterValueWh;

	@PrePersist
	void onCreate() {
		if (this.recordedAt == null) {
			this.recordedAt = OffsetDateTime.now();
		}
	}
}
