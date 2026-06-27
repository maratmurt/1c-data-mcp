# 1C HTTP Extension

## ADDED Requirements

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
