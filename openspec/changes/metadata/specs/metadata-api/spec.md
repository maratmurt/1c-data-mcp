# Metadata API

HTTP API and JSON contracts for read-only metadata discovery in the DataMcp extension.

## ADDED Requirements

### Requirement: Metadata summary endpoint

The HTTP service SHALL expose `GET /hs/datamcp/v1/metadata` returning a JSON summary of configuration metadata.

#### Scenario: Default summary mode

- **WHEN** a client sends `GET /hs/datamcp/v1/metadata` with valid Basic Auth
- **THEN** the response status is 200
- **AND** the JSON body includes `configuration`, `version`, and `counts` — an object mapping metadata type names to object counts
- **AND** `counts` includes at minimum: `Catalog`, `Document`, `Enum`, `InformationRegister`, `AccumulationRegister`

#### Scenario: Summary with explicit mode parameter

- **WHEN** a client sends `GET /hs/datamcp/v1/metadata?mode=summary`
- **THEN** the response matches the default summary format

### Requirement: Metadata list endpoint with pagination

The HTTP service SHALL support `GET /hs/datamcp/v1/metadata?mode=list` returning a paginated flat list of metadata objects.

#### Scenario: Paginated list filtered by types

- **WHEN** a client sends `GET /hs/datamcp/v1/metadata?mode=list&types=Catalog,Document&offset=0&limit=100`
- **THEN** the response status is 200
- **AND** the body contains `items` — an array of objects with fields: `type`, `name`, `synonym`, `fullName`, `queryName`
- **AND** the body contains `total`, `offset`, and `limit` reflecting pagination state
- **AND** each item's `fullName` uses the format `{type}.{name}` (e.g. `Catalog.Номенклатура`)
- **AND** each item's `queryName` uses the 1C query language prefix (e.g. `Справочник.Номенклатура`)

### Requirement: Object search endpoint

The HTTP service SHALL expose `GET /hs/datamcp/v1/objects/search` for substring search by object name and synonym.

#### Scenario: Successful search

- **WHEN** a client sends `GET /hs/datamcp/v1/objects/search?q=номенклатур&limit=20` with valid Basic Auth
- **THEN** the response status is 200
- **AND** the body contains `items` — matching objects with fields `type`, `name`, `synonym`, `fullName`, `queryName`
- **AND** results match objects where `name` or `synonym` contains the query substring (case-insensitive)
- **AND** at most `limit` items are returned (default 20)

#### Scenario: Search filtered by types

- **WHEN** a client sends `GET /hs/datamcp/v1/objects/search?q=остат&types=AccumulationRegister&limit=10`
- **THEN** only objects of the specified types are included in results

### Requirement: Object describe endpoint

The HTTP service SHALL expose `GET /hs/datamcp/v1/objects/{type}/{name}` returning structural description of a metadata object.

#### Scenario: Describe catalog object

- **WHEN** a client sends `GET /hs/datamcp/v1/objects/Catalog/Номенклатура` with valid Basic Auth
- **THEN** the response status is 200
- **AND** the body includes `type`, `name`, `synonym`, `fullName`, `queryName`
- **AND** the body includes `attributes` — array of attribute descriptors with `name`, `synonym`, `types`, and `nullable`
- **AND** the body includes `tabularSections` — array with `name`, `synonym`, and nested `attributes`

#### Scenario: Describe accumulation register with virtual tables

- **WHEN** a client sends `GET /hs/datamcp/v1/objects/AccumulationRegister/{name}` for a valid accumulation register
- **THEN** the response includes `dimensions`, `resources`, and `attributes`
- **AND** the response includes `virtualTables` listing `Остатки`, `Обороты`, and `ОстаткиИОбороты`

#### Scenario: Object not found

- **WHEN** a client requests a non-existent object type or name
- **THEN** the response status is 404
- **AND** the body includes an `error` message

#### Scenario: Unauthorized describe request

- **WHEN** a client sends a describe request without valid credentials
- **THEN** the response status is 401
