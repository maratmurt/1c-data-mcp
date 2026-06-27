# Query Security

Two-layer security model for read-only query execution: Java pre-flight guard and authoritative 1C SecuritySvc validation.

## ADDED Requirements

### Requirement: Java QueryGuard pre-flight validation

The Java MCP server SHALL validate query text before sending it to 1C.

#### Scenario: Query exceeds max length

- **WHEN** the query text length exceeds `datamcp.query.max-length`
- **THEN** the server rejects the request without calling 1C
- **AND** returns an error to the MCP caller

#### Scenario: Query must start with SELECT

- **WHEN** the trimmed query text does not start with `ВЫБРАТЬ` (case-insensitive)
- **THEN** the server rejects the request without calling 1C

#### Scenario: Forbidden tokens blocked on Java

- **WHEN** the query text contains forbidden tokens such as `РАЗРЕШИТЬ`, `ВНЕШНИЕ`, `ИЗМЕНИТЬ`, `УДАЛИТЬ`, or `ВСТАВИТЬ` (case-insensitive)
- **THEN** the server rejects the request without calling 1C

### Requirement: BSL SecuritySvc authoritative validation

The 1C extension SHALL validate query text in SecuritySvc before execution, regardless of Java pre-checks.

#### Scenario: Only SELECT queries allowed

- **WHEN** SecuritySvc receives query text for validation
- **THEN** it SHALL reject queries that are not read-only SELECT statements
- **AND** rejection occurs before `Запрос.Выполнить()`

#### Scenario: FIRST N required

- **WHEN** the query text does not contain `ПЕРВЫЕ N` in the final SELECT with N within configured max-rows
- **THEN** SecuritySvc rejects the query with a 400 error

#### Scenario: ПОМЕСТИТЬ allowed

- **WHEN** the query text contains `ПОМЕСТИТЬ` for temporary tables as part of a multi-statement read-only query
- **THEN** SecuritySvc allows execution if all other validation rules pass

#### Scenario: РАЗРЕШИТЬ forbidden

- **WHEN** the query text contains `РАЗРЕШИТЬ`
- **THEN** SecuritySvc rejects the query

### Requirement: Privileged mode for data access

The 1C extension SHALL execute validated queries in privileged mode after SecuritySvc approval.

#### Scenario: Privileged mode scoped to execution

- **WHEN** a query passes SecuritySvc validation
- **THEN** the server enables privileged mode only for the duration of `Запрос.Выполнить()`
- **AND** restores non-privileged mode afterward

#### Scenario: Privileged mode not used without validation

- **WHEN** SecuritySvc rejects a query
- **THEN** privileged mode is never enabled
- **AND** the query is not executed

### Requirement: Query audit logging with hash only

The Java MCP server SHALL log query execution events using a hash of the query text, not the full text.

#### Scenario: Audit log entry on successful query

- **WHEN** a query is successfully executed
- **THEN** the server writes an audit log entry containing: timestamp, connection name, SHA-256 hash of query text, rowCount, executionMs, and status

#### Scenario: Audit log entry on rejected query

- **WHEN** QueryGuard rejects a query before calling 1C
- **THEN** the server writes an audit log entry with the query hash and rejection status
- **AND** does not log the full query text

### Requirement: Query execution limits

The system SHALL enforce configurable limits on query size, row count, and execution time.

#### Scenario: Max rows enforced

- **WHEN** a query specifies `ПЕРВЫЕ N` where N exceeds `datamcp.query.max-rows`
- **THEN** the request is rejected with a 400 error

#### Scenario: Timeout enforced

- **WHEN** query execution exceeds `datamcp.query.timeout-seconds`
- **THEN** the request fails with a timeout error
