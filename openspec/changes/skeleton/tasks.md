## 1. Gradle Project Setup

- [ ] 1.1 Create `server/settings.gradle.kts` and `server/build.gradle.kts` with Java 17, Spring Boot 3, Spring AI MCP starter
- [ ] 1.2 Add dependencies: `spring-ai-starter-mcp-server`, `spring-boot-starter-webflux` (WebClient)
- [ ] 1.3 Create `DataMcpApplication.java` with `@SpringBootApplication`
- [ ] 1.4 Configure `application.yml`: `spring.main.web-application-type=none`, `spring.ai.mcp.server.stdio=true`, `datamcp` section
- [ ] 1.5 Verify build: `./gradlew bootJar` produces executable JAR

## 2. Connection Configuration Layer

- [ ] 2.1 Create `ConnectionProperties` and `DataMcpProperties` classes bound to `datamcp.*` prefix
- [ ] 2.2 Create `WebClientConfig` with Basic Auth filter per connection
- [ ] 2.3 Add `application.yml` connection `ut` pointing to published UT base with `${ONEC_USER}` / `${ONEC_PASSWORD}` placeholders

## 3. 1C Integration Client

- [ ] 3.1 Create DTO `PingResponse` (status, configuration, version)
- [ ] 3.2 Create `OneCClient` with method `ping(connectionName)` calling `GET /hs/datamcp/v1/ping`
- [ ] 3.3 Handle HTTP errors: 401, timeout, connection refused — return structured error

## 4. MCP Tools

- [ ] 4.1 Create `DataMcpTools` class with `@McpTool` annotation scanning
- [ ] 4.2 Implement `list_connections` tool: load connections, call ping, return JSON with `reachable` status
- [ ] 4.3 Create `ConnectionService` orchestrating config + health check

## 5. 1C Extension (CFE DataMcp)

- [ ] 5.1 Initialize extension `src/cfe/DataMcp/` with manifest and compatibility mode
- [ ] 5.2 Create HTTP service `DataMcp` with root URL `datamcp/v1`
- [ ] 5.3 Implement `GET ping` handler in HTTP service module
- [ ] 5.4 Create common module `DataMcp_Общий` with `Ping()` function returning configuration name and version
- [ ] 5.5 Create role `DataMcpReadOnly` with read-only rights

## 6. Deploy Extension to Test Base

- [ ] 6.1 Load extension into `build/ib` via db-load-xml
- [ ] 6.2 Update database (db-update) to apply extension
- [ ] 6.3 Publish web access for UT base (web-publish)
- [ ] 6.4 Verify manually: `curl -u user:pass http://localhost:<port>/<pub>/hs/datamcp/v1/ping`

## 7. Cursor Integration

- [ ] 7.1 Create `.cursor/mcp.json` entry for `1c-data` STDIO server pointing to built JAR
- [ ] 7.2 Set environment variables for ONEC_USER and ONEC_PASSWORD
- [ ] 7.3 End-to-end test: call `list_connections` from Cursor, verify `reachable: true`

## 8. Documentation

- [ ] 8.1 Add `server/README.md` with build, run, and configuration instructions
- [ ] 8.2 Document web-publish prerequisites and extension installation steps
