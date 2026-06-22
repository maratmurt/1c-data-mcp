## ADDED Requirements

### Requirement: MCP server starts over STDIO transport

The Java MCP server SHALL start with STDIO transport enabled and web application type set to none (no HTTP listener on the Java side).

#### Scenario: Server starts from JAR via STDIO

- **WHEN** the server JAR is launched with STDIO MCP configuration
- **THEN** the process reads MCP protocol messages from stdin and writes responses to stdout
- **AND** no HTTP port is opened by the Java application

### Requirement: MCP tool list_connections is registered

The MCP server SHALL expose a tool named `list_connections` that returns configured 1C database connections with reachability status.

#### Scenario: list_connections returns configured connection

- **WHEN** the AI agent calls the `list_connections` tool
- **THEN** the server returns a JSON array of connections from `application.yml`
- **AND** each connection includes fields: `name`, `url`, `default`, `reachable`
- **AND** if ping succeeds, `reachable` is `true` and `configuration` name is included

#### Scenario: list_connections reports unreachable base

- **WHEN** the AI agent calls `list_connections` and the 1C HTTP service is unavailable
- **THEN** the connection entry has `reachable` set to `false`
- **AND** an `error` field describes the failure reason

### Requirement: Server is buildable as executable JAR

The Gradle project SHALL produce an executable fat JAR via `bootJar` task.

#### Scenario: Successful build

- **WHEN** developer runs `./gradlew bootJar` in the `server/` directory
- **THEN** an executable JAR is created in `server/build/libs/`
- **AND** the JAR can be launched with `java -jar`
