# 1C HTTP Extension

Universal DataMcp configuration extension providing HTTP API for MCP integration.

## Requirements

### Requirement: Universal DataMcp extension provides HTTP service

The 1C configuration extension `DataMcp` SHALL provide an HTTP service accessible at `/hs/datamcp/v1/` that works independently of the host configuration (UT, ERP, ZUP, custom).

#### Scenario: HTTP service root is registered

- **WHEN** the DataMcp extension is installed and the infobase is published for web access
- **THEN** HTTP requests to `/hs/datamcp/v1/` are routed to the DataMcp HTTP service module

### Requirement: Ping endpoint returns health status

The HTTP service SHALL expose `GET /hs/datamcp/v1/ping` returning JSON with server health and configuration info.

#### Scenario: Successful ping

- **WHEN** a client sends `GET /hs/datamcp/v1/ping` with valid Basic Auth credentials
- **THEN** the response status is 200
- **AND** the JSON body contains `"status": "ok"`
- **AND** the body includes `configuration` name and `version` of the host configuration

#### Scenario: Unauthorized ping

- **WHEN** a client sends `GET /hs/datamcp/v1/ping` without valid credentials
- **THEN** the response status is 401

### Requirement: Read-only role for HTTP service user

The extension SHALL include a role `DataMcpReadOnly` with read-only permissions suitable for future query operations.

#### Scenario: Role exists in extension

- **WHEN** the DataMcp extension metadata is inspected
- **THEN** a role named `DataMcpReadOnly` is present
- **AND** the role does not grant write permissions to catalog or document objects

### Requirement: Metadata HTTP endpoints are registered

The DataMcp HTTP service SHALL expose metadata discovery endpoints under `/hs/datamcp/v1/` in addition to the existing ping endpoint.

#### Scenario: Metadata routes are reachable

- **WHEN** the DataMcp extension is installed and the infobase is published for web access
- **THEN** `GET /hs/datamcp/v1/metadata` is routed to the metadata handler
- **AND** `GET /hs/datamcp/v1/objects/search` is routed to the search handler
- **AND** `GET /hs/datamcp/v1/objects/{type}/{name}` is routed to the describe handler

### Requirement: Metadata endpoints require authentication

All metadata HTTP endpoints SHALL require valid HTTP Basic Auth credentials, consistent with the ping endpoint.

#### Scenario: Unauthorized metadata request

- **WHEN** a client sends a metadata API request without valid credentials
- **THEN** the response status is 401

### Requirement: Read-only role grants metadata endpoint access

The `DataMcpReadOnly` role SHALL include method-level rights for GET on all metadata URL templates.

#### Scenario: Role includes metadata method rights

- **WHEN** the DataMcpReadOnly role rights are inspected
- **THEN** GET rights are granted for metadata, objects/search, and objects/{type}/{name} URL templates
- **AND** the role does not grant write permissions to catalog or document data

### Requirement: Query HTTP endpoint is registered

The DataMcp HTTP service SHALL expose a query execution endpoint under `/hs/datamcp/v1/` in addition to existing ping and metadata endpoints.

#### Scenario: Query route is reachable

- **WHEN** the DataMcp extension is installed and the infobase is published for web access
- **THEN** `POST /hs/datamcp/v1/query` is routed to the query handler

### Requirement: Query endpoint requires authentication

The query HTTP endpoint SHALL require valid HTTP Basic Auth credentials, consistent with other DataMcp endpoints.

#### Scenario: Unauthorized query request

- **WHEN** a client sends `POST /hs/datamcp/v1/query` without valid credentials
- **THEN** the response status is 401

### Requirement: Read-only role grants query endpoint access

The `DataMcpReadOnly` role SHALL include method-level rights for POST on the query URL template.

#### Scenario: Role includes query method rights

- **WHEN** the DataMcpReadOnly role rights are inspected
- **THEN** POST rights are granted for the query URL template
- **AND** the role does not grant write permissions to catalog or document data objects
