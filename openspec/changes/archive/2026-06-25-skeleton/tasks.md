## 1. Gradle Project Setup

- [x] 1.1 Create `server/settings.gradle.kts` and `server/build.gradle.kts` with Java 17, Spring Boot 3, Spring AI MCP starter
- [x] 1.2 Add dependencies: `spring-ai-starter-mcp-server`, `spring-boot-starter-webflux` (WebClient)
- [x] 1.3 Create `DataMcpApplication.java` with `@SpringBootApplication`
- [x] 1.4 Configure `application.yml`: `spring.main.web-application-type=none`, `spring.ai.mcp.server.stdio=true`, `datamcp` section
- [x] 1.5 Verify build: `./gradlew bootJar` produces executable JAR

## 2. Connection Configuration Layer

- [x] 2.1 Create `ConnectionProperties` and `DataMcpProperties` classes bound to `datamcp.*` prefix
- [x] 2.2 Create `WebClientConfig` with Basic Auth filter per connection
- [x] 2.3 Add `application.yml` connection `ut` pointing to published UT base with `${ONEC_USER}` / `${ONEC_PASSWORD}` placeholders

## 3. 1C Integration Client

- [x] 3.1 Create DTO `PingResponse` (status, configuration, version)
- [x] 3.2 Create `OneCClient` with method `ping(connectionName)` calling `GET /hs/datamcp/v1/ping`
- [x] 3.3 Handle HTTP errors: 401, timeout, connection refused — return structured error

## 4. MCP Tools

- [x] 4.1 Create `DataMcpTools` class with `@McpTool` annotation scanning
- [x] 4.2 Implement `list_connections` tool: load connections, call ping, return JSON with `reachable` status
- [x] 4.3 Create `ConnectionService` orchestrating config + health check

## 5. 1C Extension (CFE DataMcp)

- [x] 5.1 Initialize extension `src/cfe/DataMcp/` with manifest and compatibility mode
- [x] 5.2 Create HTTP service `DataMcp` with root URL `datamcp/v1`
- [x] 5.3 Implement `GET ping` handler in HTTP service module
- [x] 5.4 Create common module `DataMcp_Общий` with `Ping()` function returning configuration name and version
- [x] 5.5 Create role `DataMcpReadOnly` with method-level rights (`HTTPService.DataMcp.URLTemplate.ping.Method.Get`)

## 6. Deploy Extension to Test Base

- [x] 6.1 Load extension into `build/ib` via db-load-xml
- [x] 6.2 Update database (db-update) to apply extension
- [x] 6.3 Publish web access for UT base — Apache `/datamcp`, `scripts/patch-datamcp-vrd.ps1` for extension HTTP service in `default.vrd`
- [x] 6.4 Verify manually: `curl -u datamcp:1 http://localhost/datamcp/hs/datamcp/v1/ping` → 200 JSON

> **Примечание (UT):** HTTP-сервис `DataMcp` и роль `DataMcpReadOnly` — в расширении `src/cfe/DataMcp/`. Права на HTTP — на уровне метода (`HTTPService.DataMcp.URLTemplate.ping.Method.Get`). Пользователю API (`datamcp`) назначается роль расширения. После перепубликации из конфигуратора запускать `scripts/patch-datamcp-vrd.ps1` (явное включение сервиса в `default.vrd`) и перезапускать Apache.

## 7. Cursor Integration

- [x] 7.1 Create `.cursor/mcp.json` entry for `1c-data` STDIO server pointing to built JAR
- [x] 7.2 Set environment variables for ONEC_USER and ONEC_PASSWORD (`datamcp` / `1`)
- [x] 7.3 End-to-end test: call `list_connections` from Cursor, verify `reachable: true`

## 8. Documentation

- [x] 8.1 Add `server/README.md` with build, run, and configuration instructions
- [x] 8.2 Document web-publish prerequisites and extension installation steps
