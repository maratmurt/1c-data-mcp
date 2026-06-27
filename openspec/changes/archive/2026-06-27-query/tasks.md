## 1. BSL SecuritySvc and Query Core (CFE)

- [x] 1.1 Implement `SecuritySvc.ПроверитьЗапрос(Текст, МаксСтрок)` — ВЫБРАТЬ only, ПЕРВЫЕ N required, forbidden tokens (РАЗРЕШИТЬ, ВНЕШНИЕ), ПОМЕСТИТЬ allowed
- [x] 1.2 Implement `ПреобразоватьПараметрыИзJSON(ПараметрыJSON)` — auto-detect types (boolean, number, ISO date, string, null, ref by uuid+type)
- [x] 1.3 Implement `СериализоватьЗначение(Значение)` — primitives, dates ISO-8601, refs as `{ uuid, type, presentation }`, null
- [x] 1.4 Implement `СериализоватьРезультатЗапроса(РезультатЗапроса)` — columns, rows, rowCount, truncated, executionMs
- [x] 1.5 Implement `ВыполнитьЗапрос(ТекстЗапроса, Параметры, МаксСтрок, ТаймаутСек)` — SecuritySvc → privileged mode → Запрос.Выполнить() → serialize

## 2. HTTP Service Handlers (CFE)

- [x] 2.1 Add URL template `query` with POST handler in `http-service.json` — template `v1/query`
- [x] 2.2 Implement `queryPost(Запрос)` — parse JSON body `{ query, parameters? }`, call `ВыполнитьЗапрос`, return JSON or 400/408 errors
- [x] 2.3 Update `DataMcpReadOnly` role — POST rights on query URL template

## 3. Deploy CFE to Test Base

- [x] 3.1 Load updated extension into `build/ib` (db-load-xml)
- [x] 3.2 Apply database update (db-update)
- [x] 3.3 Restart Apache if needed
- [x] 3.4 curl: `POST /hs/datamcp/v1/query` with `ВЫБРАТЬ ПЕРВЫЕ 10 ... Справочник.Номенклатура` → 200 with rows
- [x] 3.5 curl: query without ПЕРВЫЕ → 400
- [x] 3.6 curl: query with ПОМЕСТИТЬ multi-statement → 200 (if valid)

## 4. Java DTOs, Config, and Client

- [x] 4.1 Create DTOs: `QueryRequest`, `QueryResult`, `QueryColumn`, `RefValue`
- [x] 4.2 Create `QueryProperties` bound to `datamcp.query.*` (max-length, max-rows, timeout-seconds)
- [x] 4.3 Add `QUERY_PATH` constant to `WebClientConfig`
- [x] 4.4 Extend `OneCClient`: `executeQuery(connection, request)` with query timeout
- [x] 4.5 Add `application.yml` query section with defaults

## 5. Java Security and Service Layer

- [x] 5.1 Create `QueryGuard` — max length, must start with ВЫБРАТЬ, forbidden tokens
- [x] 5.2 Create `AuditLogger` — log timestamp, connection, SHA-256 hash, rowCount, executionMs, status (no full query text)
- [x] 5.3 Create `QueryService` — guard → audit → OneCClient.executeQuery → audit result

## 6. MCP Tool

- [x] 6.1 Implement `execute_query` tool in `DataMcpTools` — query, connection?, parameters?
- [x] 6.2 Rebuild JAR: `gradle bootJar`

## 7. End-to-End Verification

- [x] 7.1 MCP: `execute_query` with `ВЫБРАТЬ ПЕРВЫЕ 10` from `Справочник.Номенклатура` → rows with columns
- [x] 7.2 MCP: query without ПЕРВЫЕ → error from QueryGuard or SecuritySvc
- [x] 7.3 MCP: query exceeding max-length → rejected by QueryGuard without 1C call
- [x] 7.4 Verify reference fields include uuid, type, and presentation
- [x] 7.5 Add `QueryIntegrationTest` (Spring Boot test against UT, similar to MetadataIntegrationTest)

## 8. Documentation

- [x] 8.1 Update `server/README.md` — execute_query tool and query config
- [x] 8.2 Update `docs/deployment.md` — POST /query endpoint and security model
- [x] 8.3 Update `docs/mvp-roadmap.md` — mark stage 3 in progress/done when complete
