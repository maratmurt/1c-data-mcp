## Why

AI-агентам нужен безопасный и универсальный способ читать данные и метаданные из баз 1С без привязки к конкретной конфигурации. Сейчас в проекте есть только выгрузка УТ 11 — нет интеграционного слоя между MCP-протоколом и платформой 1С. Skeleton — первый итерационный шаг: поднять каркас Java MCP-сервера и 1С HTTP-расширения, проверить сквозное подключение к тестовой базе `build/ib`.

## What Changes

- Создать Gradle-проект `server/` на Java 17 + Spring Boot 3 с Spring AI MCP (STDIO transport)
- Реализовать MCP tool `list_connections` — возвращает сконфигурированные подключения к базам 1С
- Создать универсальное расширение `src/cfe/DataMcp/` с HTTP-сервисом `/hs/datamcp/v1`
- Реализовать endpoint `GET /ping` в 1С-расширении для проверки доступности
- Настроить подключение к одной тестовой базе УТ (`build/ib`) через Basic Auth
- Добавить конфигурацию `application.yml` с секцией `datamcp.connections`
- Подготовить пример конфигурации MCP для Cursor (`.cursor/mcp.json`)

## Capabilities

### New Capabilities

- `mcp-stdio-server`: Java MCP-сервер на STDIO — регистрация tools, конфигурация Spring Boot без web-порта
- `onec-http-extension`: Универсальное расширение DataMcp с HTTP-сервисом, ролью read-only и endpoint ping
- `connection-config`: Конфигурация подключений к базам 1С (имя, URL, credentials) через application.yml

### Modified Capabilities

_(нет — проект новый, существующих specs нет)_

## Impact

- **Новые директории**: `server/` (Gradle), `src/cfe/DataMcp/` (расширение 1С)
- **Зависимости**: Spring Boot 3, Spring AI MCP starter, WebClient
- **Инфраструктура**: потребуется web-publish тестовой базы УТ для HTTP-доступа к расширению
- **Конфигурация Cursor**: новая запись в `mcp.json` для STDIO-запуска JAR
- **Не затрагивается**: `src/cf/` (основная конфигурация УТ), AI_Debug
