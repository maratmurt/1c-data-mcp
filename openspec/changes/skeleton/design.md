## Context

Проект `1c-data-mcp` содержит выгрузку УТ 11.4 (`src/cf/`) и локальную ИБ (`build/ib`). Цель — универсальный MCP-сервер для безопасного чтения данных 1С через HTTP API расширения. Skeleton — первый этап: каркас без metadata/query.

Зафиксированные решения:
- Gradle, Java 17, Spring Boot 3
- MCP transport: STDIO (для Cursor)
- Аутентификация к 1С: Basic Auth
- URL расширения: `/hs/datamcp/v1`
- Тестовая база: УТ из `build/ib`
- AI_Debug вне scope

## Goals / Non-Goals

**Goals:**
- Gradle-проект `server/` собирается в исполняемый JAR
- MCP-сервер запускается через STDIO и регистрирует tool `list_connections`
- 1С-расширение `DataMcp` отвечает на `GET /hs/datamcp/v1/ping`
- Java-сервер проверяет доступность 1С через ping при вызове `list_connections`
- Конфигурация одного подключения `ut` в `application.yml`

**Non-Goals:**
- Metadata, find_objects, describe_object, execute_query (следующие changes)
- Multi-connection runtime (конфиг готов, но один connection в skeleton)
- Docker, HTTP MCP transport
- Кэш metadata
- AI_Debug интеграция

## Decisions

### 1. Spring AI MCP Server (STDIO)

**Решение:** `spring-ai-starter-mcp-server` + `spring.ai.mcp.server.stdio=true` + `spring.main.web-application-type=none`.

**Альтернатива:** ручная реализация JSON-RPC по MCP spec — отвергнута, Spring AI даёт `@McpTool` и auto-registration.

### 2. HTTP-клиент к 1С: WebClient

**Решение:** Spring WebFlux WebClient с Basic Auth filter.

**Альтернатива:** RestTemplate — устаревший; Feign — избыточен для 3 endpoint'ов.

### 3. Структура пакетов Java

```
com.onec.datamcp
├── DataMcpApplication
├── configuration/     ConnectionProperties, WebClientConfig, McpProperties
├── mcp/               DataMcpTools (@McpTool)
├── service/           ConnectionService, HealthService
├── integration/       OneCClient
└── integration/dto/   PingResponse
```

**Решение:** слои controller/security не нужны в skeleton — нет REST API на Java-стороне.

### 4. 1С-расширение как CFE

**Решение:** `src/cfe/DataMcp/` — отдельное расширение, устанавливается в любую базу.

Структура:
```
src/cfe/DataMcp/
├── HTTPServices/DataMcp/
│   └── Ext/Module.bsl       — обработчики URL
├── CommonModules/DataMcp_Общий/
│   └── Ext/Module.bsl       — Ping(), будущие сервисы
└── Roles/DataMcpReadOnly/
```

Root URL: `datamcp/v1` → полный путь `GET /hs/datamcp/v1/ping`.

### 5. Формат ответа ping

```json
{
  "status": "ok",
  "configuration": "УправлениеТорговлей",
  "version": "11.4.2.132"
}
```

### 6. MCP tool list_connections

Возвращает JSON-массив подключений с полем `reachable: true/false` (результат ping).

```json
[
  {
    "name": "ut",
    "url": "http://localhost:8080/ut",
    "default": true,
    "reachable": true,
    "configuration": "УправлениеТорговлей"
  }
]
```

### 7. Gradle multi-project (не monorepo root)

**Решение:** `server/` как самостоятельный Gradle-проект внутри репозитория. Root `settings.gradle.kts` в `server/`, не в корне репо — чтобы не смешивать с 1С XML.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| Web-publish не настроен — ping недоступен | Документировать шаг публикации в tasks; tool возвращает `reachable: false` с ошибкой |
| Spring AI MCP API нестабилен (1.x) | Зафиксировать версию в Gradle; минимальный набор tools |
| Расширение не загружено в ИБ | Отдельная задача: db-load-xml + db-update для CFE |
| Basic Auth credentials в env | Не коммитить пароли; использовать env vars в mcp.json |
| STDIO блокирует один процесс | Для skeleton достаточно; HTTP transport — в будущем change |

## Migration Plan

1. Создать расширение `src/cfe/DataMcp/`
2. Загрузить расширение в `build/ib`, обновить БД
3. Опубликовать базу (web-publish)
4. Собрать JAR: `cd server && ./gradlew bootJar`
5. Добавить запись в `.cursor/mcp.json`
6. Проверить: вызвать `list_connections` из Cursor

Rollback: удалить расширение из ИБ, убрать mcp.json entry.

## Open Questions

- Порт и имя публикации web-сервера для `build/ib` — определить при web-publish (задача в tasks)
- Версия Spring AI BOM — использовать последнюю stable 1.1.x
