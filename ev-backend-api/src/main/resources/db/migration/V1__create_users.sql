CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');

CREATE TABLE IF NOT EXISTS users (
	id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	email VARCHAR(255) NOT NULL UNIQUE,
	password_hash VARCHAR(255) NOT NULL,
	first_name VARCHAR(100),
	last_name VARCHAR(100),
	phone_number VARCHAR(30),

	role user_role NOT NULL DEFAULT 'USER',

	enabled BOOLEAN NOT NULL DEFAULT TRUE,

    balance NUMERIC(10,2) NOT NULL DEFAULT 0.00,

	created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
	updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_clients_email UNIQUE (email)
);

INSERT INTO users (email, password_hash, first_name, last_name, phone_number, role, enabled) VALUES
('bogadmin@chargeflow.com',
 '$2a$10$2ApNaleC3lG6N5H2LjS/yexDs76w2MaE8OH6NNCTPy/BBkH6gAxXm',
    'Bogdan', 'Admin', '+40758529540', 'ADMIN', TRUE);
