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
