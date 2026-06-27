# 1C HTTP Extension

## ADDED Requirements

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
