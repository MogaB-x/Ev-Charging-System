# Chargeflow Backend

Chargeflow is a Spring Boot backend for EV charging operations.

It is designed as the core service between charging stations and end users:

- it manages users, stations, connectors, sessions, and measurements
- it will consume station updates from a WebSocket server
- it will expose real-time-ready APIs so a frontend can display live session progress

## Highlights

- JWT-based authentication (`/auth/register`, `/auth/login`)
- Role-based access (`USER`, `ADMIN`)
- Account profile endpoint (`/account`)
- Station and connector APIs
- Charging session lifecycle + internal measurement ingestion
- Flyway-managed PostgreSQL schema migrations
- Unified error handling and audit logging

## Product Goal

The goal of this application is to provide a reliable backend platform for electric vehicle charging.

In practical terms, it should:

- keep charging infrastructure data consistent (stations, connectors, status)
- allow secure user access and account-level actions
- control and track charging session lifecycle
- collect telemetry that can later be streamed to users in near real time

This backend is the foundation for a full EV charging ecosystem (station communication + real-time user dashboard in frontend).

## Tech Stack

- Java 21
- Spring Boot 4 (Web MVC, Security, Data JPA, Validation)
- PostgreSQL + Flyway
- JWT
- Lombok

## Project Structure

Core packages:

- `auth` - register/login flow
- `security` - JWT filter + security rules
- `user` - account details and phone update
- `station` / `connector` - infrastructure domain
- `charging_session` - user charging session flow
- `session_mesurements` - internal measurements and status updates
- `common` - exceptions and global error handling
- `logger` - audit logging components

## What Has Been Implemented

Current implementation already covers the core backend domain:

- **Authentication and authorization**
  - user registration and login
  - JWT token generation and validation
  - role-based access for `ADMIN` vs authenticated users
- **User account module**
  - fetch current user account details
  - update phone number
- **Infrastructure module (stations + connectors)**
  - list public station data for authenticated users
  - admin create/update station and connector entities
  - validation for unique and consistent infrastructure data
- **Charging sessions module**
  - start/stop charging session
  - list user sessions and inspect a specific session
  - connector state changes during session lifecycle
- **Session measurements module**
  - ingest internal charging measurements
  - validate measurement consistency (for example non-decreasing meter values)
  - update charging session status from internal channel
- **Operational quality**
  - global API error model
  - request validation and enum parsing safety
  - structured SLF4J audit logs for auth/station/connector flows


## Security Model

- `/auth/**` - public
- `/admin/**` - `ADMIN` only
- other domain endpoints - authenticated users
- stateless JWT authentication via `JwtAuthenticationFilter`

## API Surface (High Level)

- Auth: `POST /auth/register`, `POST /auth/login`
- Account: `GET /account`, `PATCH /account/phone-number`
- Stations: `GET /stations`, `GET /stations/{stationId}`, admin create/update
- Connectors: list by station, list available, admin create/update
- Charging: start/stop session, list user sessions, fetch session by id
- Internal: add measurements, list measurements, patch session status

## What Problems This Solves

From a business and system perspective, this backend now solves:

- secure access control for EV charging features
- single source of truth for station/connector/session data
- consistent session tracking and state transitions
- safer API behavior through validation and predictable error responses
- traceability via audit logs for key operational actions

This creates a stable base for adding real-time features without losing data consistency.

## Quick Start

Set environment variables and run:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/chargeflow"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:SECRET_KEY="<BASE64_SECRET_MIN_256_BITS>"
$env:EXPIRATION_TIME="3600000"

.\mvnw.cmd clean spring-boot:run
```

Run tests:

```powershell
.\mvnw.cmd test
```

## Data Model

Flyway migrations define:

- `users` (roles, credentials, profile, balance)
- `stations`
- `connectors` (linked to stations)
- `charging_sessions` (linked to user, station, connector)
- `charging_session_measurements` (linked to charging session)

Indexes are included for session and measurement lookup patterns.

## Real-Time Integration Direction

The intended architecture is:

1. **Station -> WebSocket server**
   - station sends live charging events and meter updates
2. **WebSocket server -> Chargeflow backend**
   - backend receives/normalizes events and updates sessions + measurements
3. **Chargeflow backend -> Frontend**
   - frontend consumes APIs (and optionally push events) for live session status, energy, and costs

This means the current backend is already the domain and persistence layer, while WebSocket handling becomes the live event ingestion layer.

## Error Handling And Logging

- Global error responses are produced by `RestExceptionHandler` using `ApiError`
- Validation and parsing errors return clear `400` responses
- Domain errors map to `401`, `404`, `409`, or `500`
- Audit logs are emitted through:
  - `AuthAuditLogger`
  - `StationAuditLogger`
  - `ConnectorAuditLogger`

## Notes

- Package name `session_mesurements` contains a typo but is used consistently in the codebase.

## Next Milestones

- integrate with the WebSocket event source for station-side live data
- define event mapping contract (transaction id, connector status, meter timeline)
- expose real-time-friendly endpoints (or push channel) for frontend session monitoring
- extend audit and monitoring with metrics/alerts for charging session health

