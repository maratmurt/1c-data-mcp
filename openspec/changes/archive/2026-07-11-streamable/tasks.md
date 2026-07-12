## 1. Spring AI Upgrade (Spike)

- [x] 1.1 Bump `spring-ai-bom` in `server/build.gradle.kts` to **1.1.7** with Streamable HTTP support
- [x] 1.2 Replace `spring-ai-starter-mcp-server` with `spring-ai-starter-mcp-server-webflux`
- [x] 1.3 Run `./gradlew compileJava` — fix any API breaking changes; verify STDIO default profile still compiles

## 2. Streamable Profile

- [x] 2.1 Create `server/src/main/resources/application-streamable.yml`: `protocol: STREAMABLE`, `stdio: false`, port 8090, bind `0.0.0.0`, endpoint `/mcp`, `keep-alive-interval: 5s`
- [x] 2.2 Verify `application.yml` default profile unchanged (STDIO, `web-application-type: none`)
- [x] 2.3 Run `./gradlew bootJar` — JAR builds with both profiles

## 3. Bearer Token Auth

- [x] 3.1 Add `McpBearerAuthProperties` (`datamcp.mcp-auth.token` from `DATAMCP_TOKEN` env)
- [x] 3.2 Implement `McpBearerAuthFilter` — Bearer on POST `/mcp/**`; GET without session → 405; GET with `Mcp-Session-Id` → allow without Bearer
- [x] 3.3 Activate filter only on profile `streamable`; fail-fast if `DATAMCP_TOKEN` not set
- [x] 3.4 Unit test: valid token passes, missing/invalid token returns 401

## 4. Integration Verification

- [x] 4.1 Add integration test or smoke script for streamable MCP (initialize + `list_connections` over HTTP with bearer token)
- [x] 4.2 Regression: existing STDIO tests still pass (`./gradlew test`)
- [x] 4.3 Manual smoke: `SPRING_PROFILES_ACTIVE=streamable DATAMCP_TOKEN=... java -jar` → `list_connections`, `find_objects` with Cyrillic query

## 5. Docker HTTP Mode

- [x] 5.1 Add compose service `mcp-server-http` with `ports: ["8090:8090"]`, `restart: unless-stopped`, `SPRING_PROFILES_ACTIVE=streamable,docker` (HTTP-only compose, no STDIO service)
- [x] 5.2 Update `docker/.env.example` with `DATAMCP_TOKEN`
- [x] 5.3 Smoke: `docker compose up -d` — endpoint reachable at `http://localhost:8090/mcp` with bearer token

## 6. Agent Integration Examples

- [x] 6.1 Create `docs/codex-config.example.toml` with `url`, `bearer_token_env_var`
- [x] 6.2 Manual smoke: Codex on LAN machine connects to `http://<server-ip>:8090/mcp`, `/mcp` shows 5 tools, `list_connections` works

## 7. Documentation

- [x] 7.1 Update `docker/README.md` — HTTP mode section (`compose up`, `docker run -p 8090`, Cursor HTTP)
- [x] 7.2 Update `server/README.md` — streamable profile launch instructions
- [x] 7.3 Update `docs/deployment.md` — LAN access, firewall port 8090, bearer token setup
- [x] 7.4 Update `docs/mvp-roadmap.md` — add stage 6 (Streamable HTTP) with decisions table

## 8. OpenSpec Archive

- [x] 8.1 Run `openspec archive streamable` to merge delta specs into `openspec/specs/`

## 9. Post-archive fixes (Cursor HTTP compatibility)

- [x] 9.1 Bump Spring AI BOM to 1.1.7 (Cursor protocol `2025-11-25`)
- [x] 9.2 Add `keep-alive-interval: 5s` for SSE listener
- [x] 9.3 Fix auth filter: GET with `Mcp-Session-Id` without Bearer; GET probe without session → 405
- [x] 9.4 Remove STDIO from compose; HTTP-only `docker compose up`; `restart: unless-stopped`
- [x] 9.5 Add `scripts/mcp-cursor-handshake-probe.py` and `.cursor/mcp.json.streamable`
- [x] 9.6 Sync `openspec/specs/` and documentation with post-archive behavior
