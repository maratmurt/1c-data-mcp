## ADDED Requirements

### Requirement: list_connections reports all configured connections

The `list_connections` tool SHALL return every connection from configuration, with independent reachability per connection.

#### Scenario: Multiple connections with mixed reachability

- **WHEN** the AI agent calls `list_connections`
- **AND** configuration contains connections `ut` (reachable) and `unreachable` (HTTP service down)
- **THEN** the response includes both entries
- **AND** `ut` has `reachable: true`
- **AND** `unreachable` has `reachable: false` with an `error` field
- **AND** exactly one entry has `default: true` matching `datamcp.default-connection`

### Requirement: MCP tools reject unknown connection

All MCP tools that accept a `connection` parameter SHALL validate the name against configured connections before calling 1C.

#### Scenario: metadata with unknown connection

- **WHEN** the AI agent calls `metadata` with `connection` set to `nonexistent`
- **THEN** the tool returns an error without calling the 1C HTTP service

#### Scenario: execute_query uses default when connection omitted

- **WHEN** the AI agent calls `execute_query` without a `connection` parameter after previously using a different named connection in another tool
- **THEN** the server uses `datamcp.default-connection`, not the previously used connection name
