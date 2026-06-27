# MCP STDIO Server

Java MCP server exposing tools over STDIO transport for Cursor and other MCP clients.

## Requirements

### Requirement: MCP server starts over STDIO transport

The Java MCP server SHALL start with STDIO transport enabled and web application type set to none (no HTTP listener on the Java side).

#### Scenario: Server starts from JAR via STDIO

- **WHEN** the server JAR is launched with STDIO MCP configuration
- **THEN** the process reads MCP protocol messages from stdin and writes responses to stdout
- **AND** no HTTP port is opened by the Java application

### Requirement: MCP tool list_connections is registered

The MCP server SHALL expose a tool named `list_connections` that returns configured 1C database connections with reachability status.

#### Scenario: list_connections returns configured connection

- **WHEN** the AI agent calls the `list_connections` tool
- **THEN** the server returns a JSON array of connections from `application.yml`
- **AND** each connection includes fields: `name`, `url`, `default`, `reachable`
- **AND** if ping succeeds, `reachable` is `true` and `configuration` name is included

#### Scenario: list_connections reports unreachable base

- **WHEN** the AI agent calls `list_connections` and the 1C HTTP service is unavailable
- **THEN** the connection entry has `reachable` set to `false`
- **AND** an `error` field describes the failure reason

### Requirement: Server is buildable as executable JAR

The Gradle project SHALL produce an executable fat JAR via `bootJar` task.

#### Scenario: Successful build

- **WHEN** developer runs `./gradlew bootJar` in the `server/` directory
- **THEN** an executable JAR is created in `server/build/libs/`
- **AND** the JAR can be launched with `java -jar`

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

### Requirement: MCP tool execute_query is registered

The MCP server SHALL expose a tool named `execute_query` that executes a read-only 1C query language statement and returns serialized results.

#### Scenario: execute_query returns rows

- **WHEN** the AI agent calls `execute_query` with a valid `query` containing `ВЫБРАТЬ ПЕРВЫЕ N`
- **THEN** the server returns JSON with `columns`, `rows`, `rowCount`, `truncated`, and `executionMs`

#### Scenario: execute_query with parameters

- **WHEN** the AI agent calls `execute_query` with optional `parameters` as a JSON object string or map
- **THEN** the server binds parameters with auto-detected types before execution

#### Scenario: execute_query for default connection

- **WHEN** the AI agent calls `execute_query` without a `connection` parameter
- **THEN** the server uses the configured default connection

#### Scenario: execute_query for named connection

- **WHEN** the AI agent calls `execute_query` with `connection` set to a configured name
- **THEN** the server executes the query against that connection

#### Scenario: execute_query rejected by QueryGuard

- **WHEN** the AI agent calls `execute_query` with a query that fails Java QueryGuard validation
- **THEN** the tool returns an error without calling 1C

#### Scenario: execute_query rejected by SecuritySvc

- **WHEN** the AI agent calls `execute_query` with a query missing `ПЕРВЫЕ N`
- **THEN** the tool returns an error describing the validation failure

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
