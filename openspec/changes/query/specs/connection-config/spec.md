# Connection Config

## ADDED Requirements

### Requirement: Query limits are configurable

The MCP server configuration SHALL support a `datamcp.query` section with properties controlling query validation and execution limits.

#### Scenario: Default query limits

- **WHEN** `datamcp.query` properties are not specified in `application.yml`
- **THEN** the server uses defaults: `max-length` 10000, `max-rows` 1000, `timeout-seconds` 30

#### Scenario: Custom query limits

- **WHEN** `application.yml` sets `datamcp.query.max-length`, `max-rows`, and `timeout-seconds` to positive values
- **THEN** QueryGuard and OneCClient use those values for validation and HTTP timeout
