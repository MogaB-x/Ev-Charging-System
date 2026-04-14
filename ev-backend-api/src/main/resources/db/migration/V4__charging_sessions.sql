CREATE TYPE charging_status AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED');

CREATE TABLE IF NOT EXISTS charging_sessions (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	session_code VARCHAR(100) NOT NULL UNIQUE,

	user_id BIGINT NOT NULL,
	station_id BIGINT NOT NULL,
	connector_id BIGINT NOT NULL,

    ocpp_transaction_id VARCHAR(100),

	status charging_status NOT NULL,

	started_at TIMESTAMPTZ NOT NULL,
	ended_at TIMESTAMPTZ,

	meter_start_wh BIGINT,
	meter_stop_wh BIGINT,
	energy_consumed_kwh NUMERIC(10,3),

    average_power_kw NUMERIC(10,2),

    price_per_kwh NUMERIC(10,2),
    total_price NUMERIC(10,2),

	stop_reason VARCHAR(100),

	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

	CONSTRAINT fk_charging_sessions_user
		FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_charging_sessions_station
        FOREIGN KEY (station_id) REFERENCES stations(id),

    CONSTRAINT fk_charging_sessions_connector
        FOREIGN KEY (connector_id) REFERENCES connectors(id)
);

CREATE INDEX idx_charging_sessions_user_id
    ON charging_sessions(user_id);

CREATE INDEX idx_charging_sessions_station_id
    ON charging_sessions(station_id);

CREATE INDEX idx_charging_sessions_connector_id
    ON charging_sessions(connector_id);

CREATE INDEX idx_charging_sessions_started_at
    ON charging_sessions(started_at);
