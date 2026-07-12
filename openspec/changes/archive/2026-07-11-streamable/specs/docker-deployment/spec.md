## ADDED Requirements

### Requirement: Docker supports Streamable HTTP mode

The Docker deployment SHALL support running the MCP server in Streamable HTTP mode on port 8090 without requiring stdin (`-i`).

#### Scenario: Docker run with HTTP port

- **WHEN** developer runs `docker run -p 8090:8090 -e SPRING_PROFILES_ACTIVE=streamable,docker -e DATAMCP_TOKEN -e ONEC_USER -e ONEC_PASSWORD 1c-data-mcp-server`
- **THEN** the container starts the streamable MCP server on port 8090
- **AND** the MCP endpoint is reachable at `http://<host>:8090/mcp`
- **AND** stdin is not required

#### Scenario: Compose HTTP service

- **WHEN** developer runs `docker compose up` from `docker/` directory
- **THEN** the HTTP MCP service starts with port 8090 published
- **AND** `DATAMCP_TOKEN`, `ONEC_USER`, and `ONEC_PASSWORD` are passed via environment variables
- **AND** `host.docker.internal` resolves for 1C connections
- **AND** no STDIO service is defined in compose

### Requirement: Docker HTTP documentation covers LAN access

The Docker documentation SHALL describe Streamable HTTP mode alongside existing STDIO workflow.

#### Scenario: Developer reads docker README for HTTP mode

- **WHEN** developer reads `docker/README.md`
- **THEN** instructions cover `docker compose up` and `docker run -p 8090:8090`
- **AND** instructions list required env vars including `DATAMCP_TOKEN`
- **AND** instructions note that HTTP mode enables LAN access from remote MCP clients
- **AND** instructions document Cursor HTTP config (`.cursor/mcp.json.streamable`)
- **AND** existing STDIO documentation remains unchanged
