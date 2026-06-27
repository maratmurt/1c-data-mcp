# MVP: поэтапный план

Зафиксированные решения (из explore-сессии):

| # | Решение |
|---|---------|
| 1 | **Gradle** — `server/build.gradle.kts` |
| 2 | **STDIO** — запуск из Cursor через `command` в `mcp.json` |
| 3 | **Basic Auth** — к 1С HTTP-сервису |
| 4 | **Кэш metadata** — in-memory на Java, TTL ~15–60 мин |
| 5 | **URL** — `/hs/datamcp/v1/...` |
| 6 | **AI_Debug** — вне scope |
| 7 | **Тестовая база** — УТ из `build/ib` |

Ключевой принцип: **вся 1С-специфика — в расширении** (`src/cfe/DataMcp/`), Java-сервер — тонкий адаптер.

---

## Этапы

### Этап 1 — Skeleton ✅

- Spring Boot + `spring-ai-starter-mcp-server` (STDIO)
- `OneCClient` + ping к 1С
- MCP tool: `list_connections`
- 1С: `GET /ping`
- web-publish, роль `DataMcpReadOnly`, e2e в Cursor

OpenSpec change `skeleton` — завершён и заархивирован (`openspec/changes/archive/2026-06-25-skeleton/`).

### Этап 2 — Metadata

- 1С: `MetadataSvc` (summary + find + describe)
- HTTP: `GET /metadata`, `GET /find`, `POST /describe`
- MCP: `metadata`, `find_objects`, `describe_object`
- Кэш metadata на Java (TTL из `application.yml`)
- Тест на УТ: «найди Справочник.Номенклатура»

Metadata **не монолит** — summary + find + describe, иначе контекст агента переполнится.

### Этап 3 — Query

- 1С: `QuerySvc` + `SecuritySvc`
- HTTP: `POST /query`
- MCP: `execute_query`
- Лимиты строк, таймауты, логирование
- Двухконтурная безопасность: грубый фильтр на Java + обязательная проверка в 1С перед `Запрос.Выполнить()`
- Тест: `ВЫБРАТЬ ПЕРВЫЕ 10` из Номенклатуры

### Этап 4 — Multi-connection

- `ConnectionResolver` (или аналог в `ConnectionService`)
- Параметр `connection` во всех MCP tools
- `default-connection` в `application.yml` (каркас уже есть)

### Этап 5 — Docker

- `Dockerfile` (server)
- `docker-compose` (server + env vars)
- Документация: установка расширения + публикация
- Опционально: HTTP MCP transport (STREAMABLE) для удалённого доступа

---

## MCP Tools → HTTP mapping (целевой)

| MCP Tool | Параметры | HTTP |
|----------|-----------|------|
| `list_connections` | — | локально из `application.yml` |
| `metadata` | `connection`, `include?` | `GET /metadata` |
| `find_objects` | `connection`, `name` | `GET /find` |
| `describe_object` | `connection`, `name` | `POST /describe` |
| `execute_query` | `connection`, `query`, `parameters?` | `POST /query` |

---

## Архитектура (целевая)

```
Cursor / AI Agent
       │ MCP (STDIO)
       ▼
Java MCP Server (Spring Boot)
  mcp tools → service → integration (WebClient) → security / audit
       │ REST/JSON + Basic Auth
       ▼
1С HTTP-сервис (расширение DataMcp)
  MetadataSvc │ QuerySvc │ SecuritySvc
       │ язык запросов 1С
       ▼
  Справочники, документы, регистры
```

---

## Безопасность (для этапа 3)

```
execute_query(text)
       │
       ▼ Java QueryGuard     — forbidden keywords, max length
       ▼ AuditLogger         — connection, query hash, timestamp
       ▼ 1С SecuritySvc     — только ВЫБРАТЬ, ПЕРВЫЕ N, таймаут
       ▼ Запрос.Выполнить()
```

Пользователь 1С: отдельная учётка с ролью `DataMcpReadOnly`.

---

## Структура проекта (целевая)

```
1c-data-mcp/
├── server/                 # Gradle, Spring Boot MCP
├── src/cfe/DataMcp/        # универсальное расширение
├── docker/                 # этап 5
├── docs/
│   ├── deployment.md
│   └── mvp-roadmap.md      # этот файл
└── openspec/
    ├── specs/              # main specs
    └── changes/            # активные changes
```
