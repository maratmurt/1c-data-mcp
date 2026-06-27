# Connection Config

Configuration of 1C database connections for the MCP server.

## Requirements

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

### Requirement: Metadata cache TTL is configurable

The MCP server configuration SHALL support a `datamcp.cache.metadata-ttl-minutes` property controlling metadata index cache lifetime.

#### Scenario: Default cache TTL

- **WHEN** `metadata-ttl-minutes` is not specified in `application.yml`
- **THEN** the server uses a default TTL of 30 minutes

#### Scenario: Custom cache TTL

- **WHEN** `application.yml` sets `datamcp.cache.metadata-ttl-minutes` to a positive integer
- **THEN** the metadata index cache expires after that many minutes

### Requirement: Query limits are configurable

The MCP server configuration SHALL support a `datamcp.query` section with properties controlling query validation and execution limits.

#### Scenario: Default query limits

- **WHEN** `datamcp.query` properties are not specified in `application.yml`
- **THEN** the server uses defaults: `max-length` 10000, `max-rows` 1000, `timeout-seconds` 30

#### Scenario: Custom query limits

- **WHEN** `application.yml` sets `datamcp.query.max-length`, `max-rows`, and `timeout-seconds` to positive values
- **THEN** QueryGuard and OneCClient use those values for validation and HTTP timeout

### Requirement: Multiple connections supported

The MCP server SHALL load and expose all connections defined in `datamcp.connections` at startup.

#### Scenario: Two connections configured

- **WHEN** `application.yml` contains connections named `ut` and `prod`, each with `url`, `username`, and `password`
- **THEN** both connections are available to MCP tools by name
- **AND** `list_connections` returns an entry for each configured connection

### Requirement: Unknown connection rejected

The server SHALL reject requests that reference a connection name not present in configuration.

#### Scenario: Unknown connection name in tool call

- **WHEN** an MCP tool is called with `connection` set to a name not in `datamcp.connections`
- **THEN** the server returns an error indicating the connection is not configured
- **AND** no HTTP request is sent to 1C

#### Scenario: Missing default and no explicit connection

- **WHEN** an MCP tool is called without a `connection` parameter
- **AND** `datamcp.default-connection` is not set or is blank
- **THEN** the server returns an error indicating no connection is available
