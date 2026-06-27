# Query API

HTTP API and JSON contracts for read-only query execution in the DataMcp extension.

## Requirements

### Requirement: Query execution endpoint

The HTTP service SHALL expose `POST /hs/datamcp/v1/query` accepting a JSON body with query text and optional parameters, returning serialized query results.

#### Scenario: Successful query execution

- **WHEN** a client sends `POST /hs/datamcp/v1/query` with valid Basic Auth and body `{ "query": "ВЫБРАТЬ ПЕРВЫЕ 10 ... ИЗ Справочник.Номенклатура КАК Номенклатура" }`
- **THEN** the response status is 200
- **AND** the JSON body contains `columns` — an array of column descriptors with `name` and `type`
- **AND** the body contains `rows` — an array of row objects keyed by column name
- **AND** the body contains `rowCount`, `truncated` (boolean), and `executionMs` (integer)

#### Scenario: Query with parameters

- **WHEN** a client sends a query body with `"parameters": { "Шаблон": "%товар%" }` and the query text references `&Шаблон`
- **THEN** the server binds parameters with auto-detected 1C types before execution
- **AND** returns 200 with matching results

#### Scenario: Empty result set

- **WHEN** a valid query returns zero rows
- **THEN** the response status is 200
- **AND** `rows` is an empty array
- **AND** `rowCount` is 0

#### Scenario: Unauthorized query request

- **WHEN** a client sends a query request without valid credentials
- **THEN** the response status is 401

### Requirement: Reference values serialized with uuid and presentation

Query result serialization SHALL represent reference-type cell values as objects with `uuid`, `type`, and `presentation` fields.

#### Scenario: Catalog reference in result row

- **WHEN** a query selects a catalog reference field
- **THEN** the corresponding cell value in `rows` is an object with `uuid` (string GUID), `type` (e.g. `CatalogRef.Номенклатура`), and `presentation` (human-readable string)

#### Scenario: Empty reference

- **WHEN** a reference field is empty in a result row
- **THEN** the cell value is JSON `null`

### Requirement: Primitive type serialization

Query result serialization SHALL map 1C primitive types to JSON equivalents.

#### Scenario: Date value

- **WHEN** a query selects a date field
- **THEN** the cell value is an ISO-8601 date or datetime string

#### Scenario: Null value

- **WHEN** a field value is NULL in the result
- **THEN** the cell value is JSON `null`

### Requirement: Query request validation errors

The query endpoint SHALL return structured error responses for invalid requests.

#### Scenario: Missing query text

- **WHEN** a client sends a POST body without a `query` field or with empty query text
- **THEN** the response status is 400
- **AND** the body includes an `error` message

#### Scenario: Security validation failure

- **WHEN** the query text fails SecuritySvc validation (e.g. missing `ПЕРВЫЕ N`, forbidden construct)
- **THEN** the response status is 400
- **AND** the body includes an `error` message describing the rejection reason

#### Scenario: Query execution timeout

- **WHEN** query execution exceeds the configured timeout
- **THEN** the response status is 408 or 504
- **AND** the body includes an `error` message indicating timeout
