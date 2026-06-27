## 1. BSL Metadata Core (CFE)

- [x] 1.1 Add type mapping constants: Catalog, Document, Enum, InformationRegister, AccumulationRegister → query prefixes
- [x] 1.2 Implement `ПолучитьСводкуМетаданных()` in `DataMcp_Общий` — counts per P0 type
- [x] 1.3 Implement `ПолучитьСписокОбъектов(Типы, Смещение, Лимит)` — flat list with fullName, queryName
- [x] 1.4 Implement `НайтиОбъекты(Запрос, Типы, Лимит)` — substring search by Имя and Синоним (case-insensitive)
- [x] 1.5 Implement `ОписатьОбъект(Тип, Имя)` — overview: attributes, tabular sections, dimensions/resources, virtualTables for accumulation registers
- [x] 1.6 Implement `СериализоватьТип(ОписаниеТипов)` helper for attribute type JSON

## 2. HTTP Service Handlers (CFE)

- [x] 2.1 Add URL template `metadata` with GET handler — parse mode, types, offset, limit query params
- [x] 2.2 Add URL template `objects/search` with GET handler — parse q, types, limit
- [x] 2.3 Add URL template `objects/{type}/{name}` with GET handler — URL-decode name, return 404 on missing object
- [x] 2.4 Shared JSON response helper (Content-Type, UTF-8, error envelope)
- [x] 2.5 Update `DataMcpReadOnly` role — GET rights on new URL templates

## 3. Deploy CFE to Test Base

- [x] 3.1 Load updated extension into `build/ib` (db-load-xml)
- [x] 3.2 Apply database update (db-update)
- [x] 3.3 Run `scripts/patch-datamcp-vrd.ps1` if needed, restart Apache
- [x] 3.4 curl: `GET /hs/datamcp/v1/metadata` → 200 with counts
- [x] 3.5 curl: `GET /hs/datamcp/v1/objects/search?q=номенклатур` → matches Номенклатура
- [x] 3.6 curl: `GET /hs/datamcp/v1/objects/Catalog/Номенклатура` → attributes JSON

## 4. Java DTOs and Client

- [x] 4.1 Create DTOs: `MetadataSummary`, `ObjectRef`, `ObjectDescription`, `AttributeDescriptor`, `PagedList`
- [x] 4.2 Extend `OneCClient`: `getMetadataSummary()`, `getMetadataList()`, `describeObject(type, name)`
- [x] 4.3 Add `WebClientConfig` path constants for metadata endpoints
- [x] 4.4 Handle 404 and HTTP errors with structured error messages

## 5. Metadata Cache and Service

- [x] 5.1 Create `CacheProperties` bound to `datamcp.cache.metadata-ttl-minutes` (default 30)
- [x] 5.2 Create `MetadataIndex` — flat list holder with built-at timestamp
- [x] 5.3 Create `MetadataService` — build index from paginated list API, TTL expiry, search by substring
- [x] 5.4 Add `application.yml` cache section

## 6. MCP Tools

- [x] 6.1 Implement `metadata` tool — connection param, calls summary API
- [x] 6.2 Implement `find_objects` tool — query, types, limit; search via MetadataService cache
- [x] 6.3 Implement `describe_object` tool — object param (`Catalog.Номенклатура`), parse type.name, call describe API
- [x] 6.4 Rebuild JAR: `gradle bootJar`

## 7. End-to-End Verification

- [x] 7.1 MCP: `metadata` → summary with UT configuration name and counts
- [x] 7.2 MCP: `find_objects` query `номенклатур` → Catalog.Номенклатура in results
- [x] 7.3 MCP: `describe_object` `Catalog.Номенклатура` → attributes list
- [x] 7.4 Verify second `find_objects` uses cache (no full 1C list rebuild within TTL)

## 8. Documentation

- [x] 8.1 Update `server/README.md` — new MCP tools and cache config
- [x] 8.2 Update `docs/deployment.md` — new endpoints and role rights
