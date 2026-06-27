## ADDED Requirements

### Requirement: MCP server runs in Docker container over STDIO

The MCP server SHALL support running inside a Docker container with STDIO transport for Cursor integration via `docker run -i`.

#### Scenario: Container STDIO transport

- **WHEN** the container is started with `docker run -i` and STDIO MCP configuration
- **THEN** the process reads MCP protocol messages from stdin and writes responses to stdout
- **AND** no HTTP port is opened by the Java application
- **AND** UTF-8 encoding is configured for JVM stdout/stderr

#### Scenario: Cursor connects via docker run

- **WHEN** Cursor MCP configuration uses `command: docker` with `args` including `run`, `-i`, `--rm`, and the server image
- **AND** environment variables for 1C credentials are passed to the container
- **AND** `SPRING_PROFILES_ACTIVE=docker` is set
- **THEN** MCP tools (`list_connections`, `metadata`, etc.) function identically to local `java -jar` execution

#### Scenario: list_connections reachable from container

- **WHEN** the AI agent calls `list_connections` from a Docker-based MCP session
- **AND** 1C HTTP service is published on the host at configured ports
- **THEN** connection `ut` reports `reachable: true`
