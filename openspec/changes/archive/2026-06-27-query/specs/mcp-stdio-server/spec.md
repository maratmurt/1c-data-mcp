# MCP STDIO Server

## ADDED Requirements

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
