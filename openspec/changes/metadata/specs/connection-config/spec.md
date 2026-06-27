# Connection Config

## ADDED Requirements

### Requirement: Metadata cache TTL is configurable

The MCP server configuration SHALL support a `datamcp.cache.metadata-ttl-minutes` property controlling metadata index cache lifetime.

#### Scenario: Default cache TTL

- **WHEN** `metadata-ttl-minutes` is not specified in `application.yml`
- **THEN** the server uses a default TTL of 30 minutes

#### Scenario: Custom cache TTL

- **WHEN** `application.yml` sets `datamcp.cache.metadata-ttl-minutes` to a positive integer
- **THEN** the metadata index cache expires after that many minutes
