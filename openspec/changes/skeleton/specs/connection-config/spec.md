## ADDED Requirements

### Requirement: Connections configured via application.yml

The MCP server SHALL read 1C database connection settings from `application.yml` under the `datamcp` prefix.

#### Scenario: Single connection configured

- **WHEN** `application.yml` contains a connection named `ut` with `url`, `username`, and `password`
- **THEN** the server loads this connection at startup
- **AND** `ut` is available to MCP tools

### Requirement: Default connection is specified

The configuration SHALL support a `default-connection` property naming the connection used when no explicit connection is specified by the caller.

#### Scenario: Default connection marked

- **WHEN** `datamcp.default-connection` is set to `ut`
- **THEN** the `list_connections` response marks the `ut` connection with `"default": true`

### Requirement: Credentials loaded from environment variables

Connection credentials SHALL be overridable via environment variables to avoid committing secrets to the repository.

#### Scenario: Password from environment

- **WHEN** `application.yml` references `${ONEC_PASSWORD}` for a connection password
- **AND** the environment variable `ONEC_PASSWORD` is set at runtime
- **THEN** the server uses the environment variable value for HTTP Basic Auth to 1C

### Requirement: HTTP client uses Basic Auth

The server SHALL authenticate to the 1C HTTP service using HTTP Basic Authentication with configured username and password.

#### Scenario: Basic Auth header sent on ping

- **WHEN** the server calls `GET /hs/datamcp/v1/ping` on a configured connection
- **THEN** the request includes an `Authorization: Basic ...` header with configured credentials
