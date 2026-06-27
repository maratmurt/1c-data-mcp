## Context

Этапы 1–4 MVP реализовали полный MCP-сервер: STDIO transport, multi-connection, metadata cache, query security. Сейчас dev workflow — локальный JDK 17 + `gradle bootJar` + `java -jar` из `.cursor/mcp.json`.

1С остаётся **вне Docker**: Apache publication на хосте (`localhost:8081`, опционально `:9090`). Java MCP-сервер — тонкий HTTP-клиент к 1С.

Зафиксированные решения из explore-сессии:

| # | Решение |
|---|---------|
| 1 | **Цель** — dev setup, не production/shared server |
| 2 | **Transport** — STDIO (STREAMABLE отложен) |
| 3 | **Config** — baked `application-docker.yml`, не volume mount |
| 4 | **Registry** — локальный `docker build`, без CI publish |

## Goals / Non-Goals

**Goals:**

- Воспроизводимый dev setup: `docker build` → `docker run -i` → Cursor MCP
- Multi-stage Dockerfile (~200MB image, без Gradle в runtime)
- Docker profile с `host.docker.internal` для доступа к 1С на хосте
- Credentials через env vars, не baked в image
- UTF-8 encoding в контейнере (кириллица в STDIO)
- Документация: build, compose, Cursor integration

**Non-Goals:**

- 1С platform / infobase / Apache в Docker
- STREAMABLE HTTP MCP transport
- CI/CD, push в registry (GHCR, Docker Hub)
- Volume mount для `application.yml`
- Auth на MCP endpoint (STDIO — локальный pipe)
- Production hardening (health checks, non-root user — optional nice-to-have)

## Decisions

### 1. Multi-stage Dockerfile

**Решение:** Stage 1 — `gradle:8-jdk17` для `bootJar`; Stage 2 — `eclipse-temurin:17-jre-alpine` с JAR.

```
Stage 1: COPY server/ → gradle bootJar
Stage 2: COPY jar → ENTRYPOINT java -jar
```

**Альтернатива:** single-stage с pre-built JAR (`COPY server/build/libs/*.jar`) — проще, но требует `gradle bootJar` на хосте перед build. Multi-stage самодостаточен.

**Альтернатива:** distroless image — отвергнута: сложнее отладка для dev setup.

### 2. Baked `application-docker.yml`

**Решение:** Spring profile `docker` с фиксированными URLs:

```yaml
datamcp:
  connections:
    - name: ut
      url: http://host.docker.internal:8081/datamcp
    - name: ut-copy
      url: http://host.docker.internal:9090/datamcp2
```

Credentials — `${ONEC_USER}`, `${ONEC_PASSWORD}` (как в default profile).

**Альтернатива:** volume mount config — отвергнута по решению пользователя; добавим позже при необходимости.

### 3. `host.docker.internal` для сети

**Решение:** В `docker-compose.yml`:

```yaml
extra_hosts:
  - "host.docker.internal:host-gateway"
```

Работает на Docker Desktop (Win/Mac) и Linux (Docker 20.10+).

**Альтернатива:** `network_mode: host` — отвергнута: не работает на Docker Desktop for Windows.

### 4. STDIO через `docker run -i`

**Решение:** Cursor `mcp.json` использует `command: docker`, `args: ["run", "-i", "--rm", ...]`. Флаг `-i` обязателен для stdin/stdout MCP protocol.

**Альтернатива:** `docker compose run --rm -T` — работает, но `docker run` проще для Cursor integration.

### 5. UTF-8 в контейнере

**Решение:** `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8` в Dockerfile ENV и/или compose env. Аналог текущего `.cursor/mcp.json`.

### 6. Структура `docker/`

```
docker/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .dockerignore
└── README.md
```

`.dockerignore` исключает `build/`, `.gradle/`, `build/ib/` для быстрого context.

### 7. Пример mcp.json отдельным файлом

**Решение:** `.cursor/mcp.json.docker` — reference, не перезаписывать рабочий `mcp.json`. Dev копирует или merge вручную.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| `host.docker.internal` недоступен на старом Linux Docker | `extra_hosts: host-gateway` в compose; документировать |
| Cursor не видит tools после docker run | Проверить `-i`, `--rm`, логи stderr; smoke в tasks |
| Baked URLs не подходят для нестандартных портов | Документировать как изменить `application-docker.yml` локально |
| Image rebuild при каждом изменении Java | Dev может продолжать использовать `java -jar` напрямую; Docker — optional workflow |
| Alpine + кириллица | Temurin JRE + UTF-8 flags; smoke с `find_objects` на русском |

## Migration Plan

1. Добавить `application-docker.yml` и Docker artifacts
2. `docker build -t 1c-data-mcp-server .` из `docker/`
3. Smoke: `docker run -i --rm -e ONEC_USER -e ONEC_PASSWORD -e SPRING_PROFILES_ACTIVE=docker 1c-data-mcp-server` → Cursor `list_connections`
4. Обновить roadmap (этап 5 ✅)
5. Archive change → sync specs to main

Rollback: удалить `docker/`, `application-docker.yml`; dev workflow через `java -jar` не затронут.

## Open Questions

- Добавлять ли non-root user в Dockerfile? **Рекомендация:** отложить — dev setup, не production.
- Включать ли `docker compose` как основной способ или только `docker run` для Cursor? **Рекомендация:** оба — compose для ручного smoke, `docker run` для Cursor.
