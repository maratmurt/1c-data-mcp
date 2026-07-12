# MCP Streamable HTTP Server

Java MCP server exposing tools over Streamable HTTP transport for remote AI agents (Codex, Claude Desktop, ChatGPT, Cursor).

## ADDED Requirements

### Requirement: MCP server starts over Streamable HTTP transport

The Java MCP server SHALL support a Spring profile `streamable` that enables Streamable HTTP MCP transport on port 8090 with endpoint `/mcp`, binding to all interfaces (`0.0.0.0`).

#### Scenario: Server starts with streamable profile

- **WHEN** the server JAR is launched with `SPRING_PROFILES_ACTIVE=streamable`
- **THEN** the process listens on port 8090
- **AND** the MCP endpoint is available at `/mcp`
- **AND** STDIO transport is disabled
- **AND** the server binds to `0.0.0.0` (reachable from LAN)
- **AND** Spring AI MCP protocol is `STREAMABLE`

#### Scenario: Default profile remains STDIO

- **WHEN** the server JAR is launched without `streamable` profile
- **THEN** STDIO transport behavior is unchanged from MVP
- **AND** no HTTP port is opened

### Requirement: SSE keep-alive for Streamable HTTP

The streamable profile SHALL enable `spring.ai.mcp.server.streamable-http.keep-alive-interval`.

#### Scenario: Keep-alive configured

- **WHEN** the server starts with profile `streamable`
- **THEN** `keep-alive-interval` is set (currently `5s`)

### Requirement: Bearer token authentication on MCP endpoint

The streamable profile SHALL require Bearer token authentication on POST requests to `/mcp/**`. The expected token SHALL be read from environment variable `DATAMCP_TOKEN`.

GET requests: POST without Bearer → 401; GET without Bearer and without `Mcp-Session-Id` → 405; GET with `Mcp-Session-Id` → allowed without Bearer.

#### Scenario: Valid bearer token accepted on POST

- **WHEN** a client sends a POST request to `/mcp` with header `Authorization: Bearer <token>`
- **AND** the token matches `DATAMCP_TOKEN`
- **THEN** the request is processed normally

#### Scenario: Missing or invalid token rejected on POST

- **WHEN** a client sends a POST request to `/mcp` without `Authorization` header
- **OR** the bearer token does not match `DATAMCP_TOKEN`
- **THEN** the server returns HTTP 401 Unauthorized
- **AND** no MCP tool is executed

#### Scenario: Server fails to start without token

- **WHEN** the server starts with profile `streamable`
- **AND** `DATAMCP_TOKEN` environment variable is not set or is empty
- **THEN** the server fails to start with a clear error message

### Requirement: Streamable profile exposes same MCP tools as STDIO

The streamable profile SHALL register the same five MCP tools as the STDIO server: `list_connections`, `metadata`, `find_objects`, `describe_object`, `execute_query`.

#### Scenario: Tool list matches STDIO server

- **WHEN** an authenticated MCP client connects to the streamable server
- **THEN** the tool list includes all five tools with the same names and descriptions as STDIO mode

#### Scenario: execute_query works over HTTP

- **WHEN** an authenticated client calls `execute_query` with a valid SELECT query
- **THEN** the server returns query results via the same `QueryService` path as STDIO
- **AND** `QueryGuard` and 1C `SecuritySvc` validation apply

### Requirement: Streamable server is buildable as executable JAR

The Gradle project SHALL produce a single executable fat JAR that supports both STDIO (default) and streamable (profile) modes.

#### Scenario: Single JAR supports both profiles

- **WHEN** developer runs `./gradlew bootJar`
- **THEN** the resulting JAR supports STDIO launch (no profile)
- **AND** the same JAR supports streamable launch with `SPRING_PROFILES_ACTIVE=streamable`

### Requirement: Streamable dev documentation covers agent integration

The project SHALL document how to connect remote AI agents to the streamable MCP server.

#### Scenario: Developer configures Codex

- **WHEN** developer reads streamable documentation
- **THEN** instructions include example `~/.codex/config.toml` entry with `url` and `bearer_token_env_var`
- **AND** instructions describe setting `DATAMCP_TOKEN` in the environment
- **AND** instructions note LAN firewall requirements for port 8090

#### Scenario: Developer configures Cursor over HTTP

- **WHEN** developer reads streamable documentation
- **THEN** instructions include `.cursor/mcp.json.streamable` with `url` and Bearer header

#### Scenario: Developer smoke-tests from LAN

- **WHEN** developer starts streamable server on host A
- **AND** runs Codex on host B in the same LAN
- **THEN** Codex can connect to `http://<host-A-ip>:8090/mcp` with bearer token
- **AND** `list_connections` returns configured connections
