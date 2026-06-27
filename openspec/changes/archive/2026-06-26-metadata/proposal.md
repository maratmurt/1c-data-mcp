## Why

После skeleton AI-агент знает, что база доступна (`list_connections` + `ping`), но не видит её структуру — не может найти справочники, документы, регистры и их реквизиты. Без этого невозможно осмысленно готовить запросы на этапе 3. Metadata — второй итерационный шаг: read-only обзор метаданных живой базы через универсальное расширение DataMcp.

## What Changes

- Добавить MCP tools: `metadata`, `find_objects`, `describe_object`
- Расширить HTTP API DataMcp тремя GET-endpoint'ами: summary, search, describe
- Реализовать обход метаданных в BSL через `Метаданные.*` (P0-типы: справочники, документы, перечисления, регистры сведений и накопления)
- Добавить in-memory кэш flat-index на Java (TTL configurable) для быстрого `find_objects`
- Расширить `application.yml` секцией `datamcp.cache.metadata-ttl-minutes`
- Обновить `server/README.md` и `docs/deployment.md`

## Capabilities

### New Capabilities

- `metadata-api`: HTTP API и JSON-контракты для summary, поиска объектов и описания структуры объекта
- `metadata-cache`: In-memory кэш flat-index метаданных per connection с настраиваемым TTL

### Modified Capabilities

- `mcp-stdio-server`: регистрация tools `metadata`, `find_objects`, `describe_object`
- `onec-http-extension`: новые URL-шаблоны и обработчики metadata/search/describe
- `connection-config`: параметр `cache.metadata-ttl-minutes` в конфигурации `datamcp`

## Impact

- **Java**: `server/` — `MetadataService`, DTO, расширение `OneCClient`, `DataMcpTools`, `application.yml`
- **1С**: `src/cfe/DataMcp/` — модуль `DataMcp_Общий`, HTTP-сервис, права роли `DataMcpReadOnly`
- **Не затрагивается**: `execute_query` (этап 3), `src/cf/`, AI_Debug, Docker
- **Инфраструктура**: существующая web-публикация `build/ib`; после обновления CFE — db-load-xml + db-update
