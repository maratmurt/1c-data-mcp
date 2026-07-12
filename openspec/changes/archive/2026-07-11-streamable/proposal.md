## Why

MVP завершён: MCP-сервер доступен только через STDIO (Cursor, `docker run -i`). Для работы с AI-агентами без Cursor — OpenAI Codex, Claude Desktop, ChatGPT — нужен сетевой доступ по MCP Streamable HTTP. Это был отложенный пункт этапа 5 roadmap; целевой сценарий — агент на одной машине в LAN обращается к MCP-серверу на другой.

## What Changes

- Добавить Spring profile `streamable`: MCP Streamable HTTP на порту **8090**, endpoint `/mcp`
- Поднять Spring AI до версии с поддержкой Streamable HTTP; starter `spring-ai-starter-mcp-server-webflux`
- Добавить Bearer token auth на MCP endpoint (`DATAMCP_TOKEN` env var)
- Bind на `0.0.0.0` для доступа из LAN (не только localhost)
- STREAMABLE HTTP mode (stateful sessions, SSE GET listener, `keep-alive-interval: 5s`)
- Cursor: HTTP `.cursor/mcp.json.streamable` (STDIO через `mcp.json.docker` опционально)
- Добавить `application-streamable.yml` и Docker HTTP mode (`-p 8090:8090`, без `-i`)
- Документация: подключение Codex/Claude Desktop, пример `~/.codex/config.toml`, firewall notes
- Обновить `docs/mvp-roadmap.md` (этап 6)

**Не меняется:** STDIO profile (`docker run -i`), расширение `DataMcp`, 1С HTTP API, MCP tools API, OAuth на MCP, TLS/HTTPS, production hardening.

## Capabilities

### New Capabilities

- `mcp-streamable-server`: MCP Streamable HTTP transport, Bearer auth, LAN bind, порт 8090, те же 5 tools

### Modified Capabilities

- `docker-deployment`: HTTP mode в Docker (port 8090, profile `streamable`), smoke без stdin

## Impact

- **Java**: `build.gradle.kts` (Spring AI BOM bump, webflux MCP starter), `application-streamable.yml`, auth filter/config
- **Docker**: `docker-compose.yml` (service profile), `Dockerfile` (без изменений JAR), `docker/README.md`
- **Документация**: `server/README.md`, `docs/deployment.md`, `docs/mvp-roadmap.md`, пример Codex config
- **Не затрагивается**: `src/cfe/DataMcp/`, `DataMcpTools.java` (логика tools), STDIO profile
