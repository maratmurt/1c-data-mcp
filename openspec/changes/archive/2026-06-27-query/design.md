## Context

Metadata завершён: MCP tools `metadata`, `find_objects`, `describe_object` работают; расширение `DataMcp` отдаёт структуру объектов и поле `queryName` (например `Справочник.Номенклатура`) для языка запросов 1С. Агент может построить текст запроса, но не может получить данные.

Query — этап 3 MVP: безопасное read-only выполнение запросов через `POST /hs/datamcp/v1/query` и MCP tool `execute_query`.

Зафиксированные решения (из explore):
- Привилегированный режим в 1С после проверки SecuritySvc
- `ПОМЕСТИТЬ` (временные таблицы) разрешён
- Ссылки в JSON: `{ uuid, type, presentation }`
- Параметры запроса: автоопределение типов из JSON
- Аудит на Java: только SHA-256 hash запроса, без полного текста
- Двухконтурная безопасность: Java QueryGuard + обязательная BSL SecuritySvc

## Goals / Non-Goals

**Goals:**
- MCP tool `execute_query` с параметрами `query`, `connection?`, `parameters?`
- HTTP `POST /hs/datamcp/v1/query` с JSON телом `{ query, parameters? }`
- BSL: `ВыполнитьЗапрос()`, `SecuritySvc`, сериализация результата
- Java: `QueryService`, `QueryGuard`, `AuditLogger`, конфиг `datamcp.query.*`
- Лимиты: max-length текста, max-rows, timeout
- E2E на УТ: `ВЫБРАТЬ ПЕРВЫЕ 10` из `Справочник.Номенклатура`

**Non-Goals:**
- Multi-connection runtime improvements (этап 4)
- Docker, HTTP MCP transport (этап 5)
- AI_Debug интеграция
- Запись/изменение данных (INSERT/UPDATE/DELETE — не существуют в языке запросов, но блокируются явно)
- Кэширование результатов query на Java
- OData как источник данных

## Decisions

### 1. POST endpoint для query

**Решение:**
```
POST /hs/datamcp/v1/query
Content-Type: application/json

{ "query": "ВЫБРАТЬ ПЕРВЫЕ 10 ...", "parameters": { "Шаблон": "%товар%" } }
```

**Альтернатива:** GET с query в параметре — отвергнута: текст запроса слишком длинный, небезопасно в URL.

### 2. Двухконтурная безопасность

**Решение:**

```
execute_query(text)
       │
       ▼ Java QueryGuard        max length, must start with ВЫБРАТЬ, forbidden tokens
       ▼ AuditLogger            connection + SHA-256(query) + timestamp + status
       ▼ POST /query
       ▼ BSL SecuritySvc        authoritative: ВЫБРАТЬ only, ПЕРВЫЕ N, no РАЗРЕШИТЬ
       ▼ УстановитьПривилегированныйРежим(Истина)
       ▼ Запрос.Выполнить()
       ▼ УстановитьПривилегированныйРежим(Ложь)
```

Java не заменяет 1С-проверку — defense in depth.

**Forbidden на обоих уровнях:** `РАЗРЕШИТЬ`, `ВНЕШНИЕ`, `ИЗМЕНИТЬ`, `УДАЛИТЬ`, `ВСТАВИТЬ` (защита от инъекций чужого синтаксиса).

**Разрешено:** `ПОМЕСТИТЬ` для многошаговых аналитических запросов; пакеты через `;` допустимы, если финальный оператор — `ВЫБРАТЬ ПЕРВЫЕ N`.

### 3. Привилегированный режим

**Решение:** `DataMcpReadOnly` даёт только права на HTTP-методы. Чтение данных — в `УстановитьПривилегированныйРежим(Истина)` строго после успешной `SecuritySvc.ПроверитьЗапрос()`.

**Альтернатива:** явные права на объекты УТ — отвергнута: неуниверсально, требует настройки под каждую конфигурацию.

**Trade-off:** RLS конфигурации не ограничивает результат; единственный барьер — SecuritySvc whitelist.

### 4. Обязательный лимит строк

**Решение:** запрос MUST содержать `ПЕРВЫЕ N`, где `N ≤ datamcp.query.max-rows` (default 1000). Если отсутствует — SecuritySvc возвращает 400 с понятной ошибкой.

**Альтернатива:** автоматически дописывать `ПЕРВЫЕ N` — отвергнута для MVP: непредсказуемо для агента; явное требование проще отлаживать.

### 5. JSON-контракт ответа

```json
{
  "columns": [
    { "name": "Наименование", "type": "String" }
  ],
  "rows": [
    { "Наименование": "Товар 1", "Ссылка": { "uuid": "...", "type": "CatalogRef.Номенклатура", "presentation": "Товар 1" } }
  ],
  "rowCount": 10,
  "truncated": false,
  "executionMs": 42
}
```

- Пустая ссылка → `null`
- Дата → ISO-8601 строка
- Число, строка, булево → как в JSON
- NULL → `null`

### 6. Параметры — автоопределение типов

**Решение:** JSON value → 1С тип без явного type в контракте:

| JSON | 1С |
|------|-----|
| boolean | Булево |
| number (integer) | Число |
| number (float) | Число |
| string matching ISO date | Дата |
| string | Строка |
| null | Неопределено |
| `{ "uuid": "...", "type": "CatalogRef.X" }` | ссылка по типу + UUID |

При неоднозначности (например `"001"`) — строка. Ошибка преобразования → 400 с именем параметра.

### 7. Аудит

**Решение:** `AuditLogger` на Java пишет structured log:
`timestamp | connection | queryHash (SHA-256) | rowCount | executionMs | status`

Полный текст запроса не логируется. Опциональный dev-флаг `datamcp.audit.log-query-text` — вне MVP.

### 8. Конфигурация

```yaml
datamcp:
  query:
    max-length: 10000      # символов текста запроса
    max-rows: 1000         # верхняя граница ПЕРВЫЕ N
    timeout-seconds: 30    # WebClient + ориентир для 1С
```

### 9. Структура Java-пакетов

```
com.onec.datamcp
├── integration/
│   ├── OneCClient              + executeQuery()
│   └── dto/                    QueryRequest, QueryResult, QueryColumn, RefValue
├── service/
│   └── QueryService            guard → audit → client
├── security/
│   ├── QueryGuard              pre-flight validation
│   └── AuditLogger             hash-based audit
├── configuration/
│   └── QueryProperties         max-length, max-rows, timeout
└── mcp/
    └── DataMcpTools            + executeQuery()
```

### 10. BSL-модули CFE

`DataMcp_Общий`:
- `ВыполнитьЗапрос(ТекстЗапроса, Параметры)` — оркестрация
- `SecuritySvc.ПроверитьЗапрос(Текст, МаксСтрок)` — валидация текста
- `ПреобразоватьПараметрыИзJSON(ПараметрыJSON)` — автоопределение типов
- `СериализоватьРезультатЗапроса(РезультатЗапроса)` — columns + rows

HTTP: `queryPost(Запрос)` — парсинг JSON тела, вызов `ВыполнитьЗапрос`, `ОтветJSON`.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| Привилегированный режим обходит RLS | SecuritySvc — единственный gate; только SELECT; audit hash |
| Тяжёлый запрос без ПЕРВЫЕ в подзапросе | SecuritySvc проверяет наличие ПЕРВЫЕ в финальном SELECT; timeout |
| Автоопределение типов параметров ошибается | 400 с именем параметра; агент может уточнить формат |
| ПОМЕСТИТЬ + сложные пакеты | Разрешены; лимит max-length; timeout |
| Большой JSON-ответ переполняет контекст агента | max-rows default 1000; truncated flag |
| Спецсимволы в тексте запроса | UTF-8 end-to-end; McpStringEncoding на Java |

## Migration Plan

1. Реализовать BSL SecuritySvc + ВыполнитьЗапрос + сериализация в CFE
2. Добавить HTTP handler POST query, обновить права роли
3. Загрузить CFE в `build/ib`, db-update, restart Apache
4. Реализовать Java слой (QueryGuard, QueryService, OneCClient, MCP tool)
5. curl-тест POST /query на УТ
6. MCP e2e: `execute_query` с `ВЫБРАТЬ ПЕРВЫЕ 10` из Номенклатуры
7. Негативные тесты: без ПЕРВЫЕ, forbidden token, превышение max-length

Rollback: откатить CFE к версии metadata-only; убрать `execute_query` из MCP tools.

## Open Questions

_(нет — решения зафиксированы в explore-сессии)_
