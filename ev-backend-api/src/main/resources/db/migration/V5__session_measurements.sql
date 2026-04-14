CREATE TABLE IF NOT EXISTS charging_session_measurements (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	charging_session_id BIGINT NOT NULL,
	recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

	power_kw NUMERIC(10,2) NOT NULL,
	voltage_v NUMERIC(10,2),
	current_a NUMERIC(10,2),
	meter_value_wh BIGINT,

	CONSTRAINT fk_measurements_charging_session
		FOREIGN KEY (charging_session_id) REFERENCES charging_sessions(id)
			ON DELETE CASCADE
);

CREATE INDEX idx_charging_session_measurements_session_id
	ON charging_session_measurements(charging_session_id);

CREATE INDEX idx_charging_session_measurements_recorded_at
	ON charging_session_measurements(recorded_at);
