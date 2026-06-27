## ADDED Requirements

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
