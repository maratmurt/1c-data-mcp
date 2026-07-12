## Context

MVP (этапы 1–5) реализовал MCP-сервер с STDIO transport для Cursor. Streamable HTTP был явно отложен в change `docker`. Теперь нужен сетевой доступ для AI-агентов (OpenAI Codex, Claude Desktop, ChatGPT) без привязки к Cursor.

1С остаётся вне Docker: Apache publication на хосте. Java MCP-сервер — тонкий адаптер с теми же 5 tools.

Зафиксированные решения из explore-сессии:

| # | Решение |
|---|---------|
| 1 | **Deployment** — LAN (bind `0.0.0.0`, доступ с других машин в сети) |
| 2 | **Auth** — Bearer token (`DATAMCP_TOKEN` env var) |
| 3 | **Port** — `8090`, endpoint `/mcp` |
| 4 | **Sessions** — STREAMABLE (stateful, SSE GET listener) |
| 5 | **Cursor** — HTTP URL в `.cursor/mcp.json` (STDIO через `mcp.json.docker` опционально) |

## Goals / Non-Goals

**Goals:**

- MCP Streamable HTTP на порту 8090, endpoint `/mcp`
- Bearer token auth на всех MCP HTTP requests
- Bind `0.0.0.0` для LAN access
- Spring profile `streamable` — отдельный от default (STDIO) и `docker`
- Те же 5 MCP tools через `DataMcpTools` (без дублирования логики)
- Docker HTTP mode: `docker run -p 8090:8090` (без `-i`)
- Документация: Codex `config.toml`, Claude Desktop Connectors, smoke test
- Spring AI BOM bump до версии с Streamable HTTP support

**Non-Goals:**

- TLS/HTTPS (LAN MVP; reverse proxy — на усмотрение админа)
- OAuth 2.1 на MCP endpoint
- Dual transport в одном процессе (STDIO + HTTP одновременно)
- Изменения в 1С extension `DataMcp`
- Production hardening (rate limiting, non-root, health checks)
- Свой Web UI / REST API поверх tools
- CI registry publish

## Decisions

### 1. Отдельный Spring profile `streamable`

**Решение:** Profile `streamable` включает HTTP listener и отключает STDIO. Default profile (без profile / `stdio`) остаётся как сейчас.

```yaml
# application-streamable.yml
spring:
  main:
    web-application-type: reactive
  ai:
    mcp:
      server:
        stdio: false
        protocol: STREAMABLE
        streamable-http:
          mcp-endpoint: /mcp
          keep-alive-interval: 5s
server:
  port: 8090
  address: 0.0.0.0
```

**Альтернатива:** один процесс с STDIO + HTTP — отвергнута: Spring AI конфликтует (`web-application-type=none` vs reactive), усложняет lifecycle.

### 2. Spring AI version bump + WebFlux starter

**Решение:** `spring-ai-bom` **1.1.7** (поддержка protocol `2025-11-25` для Cursor). Starter: `spring-ai-starter-mcp-server-webflux`.

**Альтернатива:** WebMVC starter — отвергнута: проект уже на WebFlux для `OneCClient`.

**Риск:** API changes между 1.0.0 и 1.1.0 — первый task = spike + compile.

### 3. Bearer token auth

**Решение:** Spring `WebFilter` проверяет `Authorization: Bearer <token>` на POST `/mcp/**`. Token из env `DATAMCP_TOKEN`. Fail-fast если env не задан.

**Cursor/Claude quirk (post-archive):** клиенты не отправляют Bearer на GET SSE. Правила:

- POST без Bearer → 401
- GET без Bearer и без `Mcp-Session-Id` → 405 (SSE probe)
- GET с `Mcp-Session-Id` (после authenticated initialize) → разрешён без Bearer

**Альтернатива:** bind localhost only без token — отвергнута: пользователь выбрал LAN + Bearer.

**Альтернатива:** OAuth 2.1 — отвергнута для MVP.

### 4. STREAMABLE mode (stateful sessions)

**Решение:** `protocol: STREAMABLE` — stateful sessions с optional SSE GET listener. `keep-alive-interval: 5s` для поддержания SSE stream (Cursor handshake).

**Альтернатива:** STATELESS — отвергнута для Cursor: клиент ожидает GET SSE или корректный fallback; STATELESS + auth на GET проблематичен.

### 5. Docker: HTTP-only compose

**Решение:** Единственный compose service `mcp-server-http` (без profile `http`, без STDIO service). `restart: unless-stopped`, `container_name: 1c-data-mcp-http`.

```yaml
mcp-server-http:
  ports:
    - "8090:8090"
  restart: unless-stopped
  environment:
    SPRING_PROFILES_ACTIVE: streamable,docker
    DATAMCP_TOKEN: ${DATAMCP_TOKEN}
```

STDIO остаётся только через `docker run -i` (см. `mcp.json.docker`).

`application-streamable.yml` + `application-docker.yml` compose через multi-profile.

### 6. Документация Codex

**Решение:** Пример `docs/codex-config.example.toml` (не перезаписывать `~/.codex/config.toml`):

```toml
[mcp_servers.1c-data]
url = "http://192.168.x.x:8090/mcp"
bearer_token_env_var = "DATAMCP_TOKEN"
```

### 7. Структура auth filter

**Решение:** Отдельный класс `McpBearerAuthFilter` в `server/src/main/java/.../security/`, активируется только при profile `streamable`. Не тянуть полный Spring Security.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| Spring AI 1.1.0 breaking changes | Spike task первым; integration test compile |
| HTTP без TLS в LAN | Документировать: token в plaintext HTTP; рекомендовать VPN или reverse proxy с TLS |
| Token leak в logs | Не логировать Authorization header; redact в debug |
| Firewall блокирует 8090 | Документировать Windows Firewall rule |
| Кириллица в HTTP JSON | UTF-8 by default; smoke `find_objects` с русским query |
| LAN exposure без rate limit | Scope = dev/internal; документировать риск |
| Codex remote executor не видит LAN IP | Документировать: MCP server IP must be reachable from agent host |

## Migration Plan

1. Bump Spring AI, добавить `application-streamable.yml`, auth filter
2. `gradle bootJar` — verify STDIO profile still works (regression)
3. `SPRING_PROFILES_ACTIVE=streamable java -jar` — smoke `list_connections` via MCP HTTP client
4. Docker: `docker compose up -d` — smoke from LAN machine
5. Codex: add server, `/mcp` verify tools
6. Cursor: `.cursor/mcp.json.streamable`, reload MCP, `mcp-cursor-handshake-probe.py`
7. Update roadmap (этап 6 ✅)
8. Archive change → sync specs

Rollback: удалить `application-streamable.yml`, auth filter, compose profile; STDIO workflow не затронут.

## Open Questions

- Точная версия Spring AI BOM после spike — **1.1.7** (Cursor protocol `2025-11-25`)
- `.cursor/mcp.json.streamable` reference file — **добавлен**
