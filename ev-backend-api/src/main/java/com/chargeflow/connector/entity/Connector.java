package com.chargeflow.connector.entity;

import com.chargeflow.station.entity.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
		name = "connectors",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uq_station_connector_number",
						columnNames = {"station_id", "connector_number"}
				)
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Connector {

	@Id
	@Setter(AccessLevel.NONE)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(
			name = "station_id",
			nullable = false,
			foreignKey = @ForeignKey(name = "fk_connectors_station")
	)
	private Station station;

	@Column(name = "connector_number", nullable = false)
	private Integer connectorNumber;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "connector_type", nullable = false)
	private ConnectorType connectorType;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.NAMED_ENUM)
	@Column(name = "connector_status", nullable = false)
	private ConnectorStatus connectorStatus;

	@Column(name = "max_power_kw", precision = 10, scale = 2)
	private BigDecimal maxPowerKw;

	@Column(name = "price_per_kw", precision = 10, scale = 2)
	private BigDecimal pricePerKw;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@PrePersist
	void onCreate() {
		var now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}
}
