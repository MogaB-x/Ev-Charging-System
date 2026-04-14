CREATE TYPE connector_type AS ENUM ('TYPE2', 'CSS2', 'CHADEMO', 'TESLA');
CREATE TYPE connector_status AS ENUM ('AVAILABLE', 'CHARGING','PREPARING', 'FAILURE');

CREATE TABLE IF NOT EXISTS connectors (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    station_id BIGINT NOT NULL,
    connector_number INTEGER NOT NULL,
    connector_type connector_type,
    connector_status connector_status NOT NULL,
    max_power_kw NUMERIC(10,2),
    price_per_kw DECIMAL(10,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_connectors_station
        FOREIGN KEY (station_id) REFERENCES stations(id)
            ON DELETE CASCADE,
    CONSTRAINT uq_station_connector_number
        UNIQUE (station_id, connector_number)
);