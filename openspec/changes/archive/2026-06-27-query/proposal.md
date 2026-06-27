## Why

После metadata AI-агент знает структуру базы (`find_objects`, `describe_object`, `queryName`), но не может прочитать данные. Query — третий итерационный шаг MVP: безопасное выполнение read-only запросов на языке запросов 1С через MCP tool `execute_query`. Без этого агент не может ответить на вопросы вроде «покажи 10 номенклатур» или «какие остатки по складу».

## What Changes

- Добавить MCP tool `execute_query` с параметрами `query`, `connection?`, `parameters?`
- Расширить HTTP API DataMcp: `POST /hs/datamcp/v1/query`
- Реализовать в BSL: `ВыполнитьЗапрос()`, `SecuritySvc` (проверка текста запроса), сериализация результата в JSON
- Добавить на Java: `QueryService`, `QueryGuard` (грубый фильтр), `AuditLogger` (hash запроса)
- Расширить `application.yml` секцией `datamcp.query` (max-length, max-rows, timeout)
- Двухконтурная безопасность: Java QueryGuard + обязательная проверка в 1С перед `Запрос.Выполнить()`
- Выполнение в привилегированном режиме после успешной проверки SecuritySvc
- Тест на УТ: `ВЫБРАТЬ ПЕРВЫЕ 10` из `Справочник.Номенклатура`

## Capabilities

### New Capabilities

- `query-api`: HTTP API `POST /query`, JSON-контракт запроса/ответа, сериализация типов 1С (включая ссылки с uuid + presentation)
- `query-security`: двухконтурная защита (Java QueryGuard + BSL SecuritySvc), лимиты строк, таймауты, аудит по hash

### Modified Capabilities

- `mcp-stdio-server`: регистрация tool `execute_query`
- `onec-http-extension`: URL-шаблон `query` (POST), обработчик, права роли `DataMcpReadOnly`
- `connection-config`: параметры `datamcp.query.*` в конфигурации

## Impact

- **Java**: `server/` — `QueryService`, `QueryGuard`, `AuditLogger`, DTO (`QueryRequest`, `QueryResult`), расширение `OneCClient`, `DataMcpTools`, `application.yml`
- **1С**: `src/cfe/DataMcp/` — `DataMcp_Общий` (`ВыполнитьЗапрос`, `SecuritySvc`, сериализация), HTTP-сервис, права роли
- **Не затрагивается**: `src/cf/`, multi-connection runtime (этап 4), Docker (этап 5), AI_Debug
- **Инфраструктура**: обновление CFE в `build/ib`, db-load-xml + db-update, curl/MCP e2e-тесты
