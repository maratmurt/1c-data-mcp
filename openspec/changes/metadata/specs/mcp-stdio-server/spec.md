# MCP STDIO Server

## ADDED Requirements

### Requirement: MCP tool metadata is registered

The MCP server SHALL expose a tool named `metadata` that returns a summary of configuration metadata for a selected connection.

#### Scenario: metadata returns summary for default connection

- **WHEN** the AI agent calls `metadata` without a connection parameter
- **THEN** the server uses the configured default connection
- **AND** returns JSON with `configuration`, `version`, and per-type object counts

#### Scenario: metadata for named connection

- **WHEN** the AI agent calls `metadata` with `connection` set to a configured name
- **THEN** the server returns metadata summary for that connection

#### Scenario: metadata reports unreachable connection

- **WHEN** the AI agent calls `metadata` and the 1C HTTP service is unavailable
- **THEN** the tool returns an error describing the failure

### Requirement: MCP tool find_objects is registered

The MCP server SHALL expose a tool named `find_objects` that searches metadata objects by substring in name and synonym.

#### Scenario: find_objects returns matches

- **WHEN** the AI agent calls `find_objects` with `query` set to a search string
- **THEN** the server returns a JSON array of matching objects
- **AND** each match includes `type`, `name`, `synonym`, `fullName`, and `queryName`
- **AND** at most `limit` results are returned (default 20)

#### Scenario: find_objects with type filter

- **WHEN** the AI agent calls `find_objects` with optional `types` parameter (e.g. `Catalog,Document`)
- **THEN** only objects of the specified types are included

### Requirement: MCP tool describe_object is registered

The MCP server SHALL expose a tool named `describe_object` that returns structural description of a metadata object.

#### Scenario: describe_object by fullName

- **WHEN** the AI agent calls `describe_object` with `object` set to `Catalog.Номенклатура`
- **THEN** the server returns JSON with attributes, tabular sections, and type information at overview level

#### Scenario: describe_object for unknown object

- **WHEN** the AI agent calls `describe_object` with a non-existent object identifier
- **THEN** the tool returns an error indicating the object was not found
