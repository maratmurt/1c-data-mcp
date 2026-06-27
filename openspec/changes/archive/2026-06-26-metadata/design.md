## Context

Skeleton завершён: Java MCP-сервер (`list_connections`), расширение `DataMcp` (`GET /hs/datamcp/v1/ping`), тестовая база УТ 11.4 в `build/ib`. Агент может проверить доступность базы, но не знает её структуру.

Metadata — этап 2: read-only обзор метаданных через рефлексию `Метаданные.*` в BSL. УТ содержит тысячи объектов — полный дамп в одном ответе неприемлем по размеру и времени.

Зафиксированные решения (из explore):
- `metadata` по умолчанию возвращает **summary** (счётчики по типам)
- `find_objects` — подстрока по `Имя` и `Синоним` (`СтрНайти`)
- In-memory **flat-index cache** на Java, TTL 30 мин (configurable)
- P0-типы: Catalog, Document, Enum, InformationRegister, AccumulationRegister
- `describe_object` level **overview**; для регистров накопления — виртуальные таблицы
- `execute_query`, Docker, AI_Debug — вне scope

## Goals / Non-Goals

**Goals:**
- MCP tools `metadata`, `find_objects`, `describe_object` с параметром `connection` (default из конфига)
- HTTP API в CFE: `GET /metadata`, `GET /objects/search`, `GET /objects/{type}/{name}`
- Стабильный JSON-контракт с полями `fullName`, `queryName`, `type`, `name`, `synonym`
- Кэш flat-index на Java для быстрого поиска без повторного обхода 1С
- Проверка на УТ: `Catalog.Номенклатура`, `Catalog.Контрагенты`

**Non-Goals:**
- `execute_query` (этап 3)
- `metadata` mode `list` с полным дампом всех объектов без пагинации
- Fuzzy/translit search, embeddings
- P2-типы (Plans, BusinessProcesses, Reports, DataProcessors)
- `describe` level `full` (формы, движения документов)
- Кэширование `describe` на Java
- OData как источник metadata

## Decisions

### 1. Три HTTP endpoint'а (GET only)

**Решение:**
```
GET /hs/datamcp/v1/metadata?mode=summary|list&types=Catalog,Document&offset=0&limit=100
GET /hs/datamcp/v1/objects/search?q=номенклатур&types=Catalog&limit=20
GET /hs/datamcp/v1/objects/{type}/{name}
```

**Альтернатива:** `POST /describe` с телом — отвергнута: GET проще, read-only, кэшируем summary.

### 2. Summary-first metadata

**Решение:** `mode=summary` (default) — configuration, version, counts per type.

`mode=list` — плоский список `{type, name, synonym, fullName, queryName}` с фильтром `types` и пагинацией `offset`/`limit`. Используется Java-сервером для построения index cache.

**Альтернатива:** отдавать всё дерево — отвергнута из-за размера УТ.

### 3. Flat-index cache на Java

**Решение:** `MetadataService` при первом `find_objects` или явном cache warm вызывает 1С `metadata?mode=list` (все P0-типы), сохраняет в `ConcurrentHashMap` per connection. TTL из `datamcp.cache.metadata-ttl-minutes` (default 30).

`find_objects` ищет по кэшу в Java. `describe_object` всегда идёт в 1С (актуальная структура).

**Альтернатива:** поиск в BSL на каждый запрос — отвергнута: 2–5 сек на УТ при каждом find.

### 4. Именование объектов

| Поле | Пример | Назначение |
|------|--------|------------|
| `type` | `Catalog` | Англ. имя коллекции |
| `name` | `Номенклатура` | Программное имя |
| `synonym` | `Номенклатура` | Для поиска по-русски |
| `fullName` | `Catalog.Номенклатура` | Канон для MCP tools |
| `queryName` | `Справочник.Номенклатура` | Для этапа 3 (язык запросов) |

Маппинг `type` → префикс запроса в BSL (`Catalog` → `Справочник`).

### 5. P0-коллекции метаданных в BSL

```
Метаданные.Справочники          → Catalog
Метаданные.Документы            → Document
Метаданные.Перечисления         → Enum
Метаданные.РегистрыСведений     → InformationRegister
Метаданные.РегистрыНакопления   → AccumulationRegister
```

### 6. Describe overview

Для справочников/документов/перечислений:
- `attributes[]`: name, synonym, types[], nullable
- `tabularSections[]`: name, attributes[]

Для регистров:
- `dimensions[]`, `resources[]`, `attributes[]`
- `virtualTables[]` (только AccumulationRegister): `Остатки`, `Обороты`, `ОстаткиИОбороты`

Сериализация типов: массив строк для простых (`CatalogRef.Контрагенты`), объект для `Number` (`digits`, `fraction`).

### 7. Структура Java-пакетов

```
com.onec.datamcp
├── integration/
│   ├── OneCClient          + getMetadataSummary(), getMetadataList(), searchObjects(), describeObject()
│   └── dto/                MetadataSummary, ObjectRef, ObjectDescription, TypeDescriptor
├── service/
│   ├── MetadataService     cache + search logic
│   └── MetadataIndex       flat list holder
├── mcp/
│   └── DataMcpTools        + metadata, findObjects, describeObject
└── configuration/
    └── CacheProperties     metadata-ttl-minutes
```

### 8. BSL-модули CFE

`DataMcp_Общий`:
- `ПолучитьСводкуМетаданных()`
- `ПолучитьСписокОбъектов(Типы, Смещение, Лимит)`
- `НайтиОбъекты(Запрос, Типы, Лимит)` — для прямого HTTP search (fallback)
- `ОписатьОбъект(Тип, Имя)`
- `СериализоватьТип(ОписаниеТипов)`

HTTP handlers в `HTTPServices/DataMcp/Ext/Module.bsl`.

### 9. Права роли DataMcpReadOnly

Добавить права на новые URL-шаблоны (method GET) аналогично ping.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| Первый find медленный (построение index) | Документировать; TTL 30 мин; опциональный warm при старте — не в MVP |
| Index устаревает после обновления конфигурации | TTL; перезапуск MCP-сервера |
| Большой list при cache warm | Пагинация на стороне 1С; Java собирает постранично |
| Сериализация составных типов сложна | MVP: строковое представление типа; structured — для Number/Date |
| Спецсимволы в имени объекта в URL | URL-encode на Java; BSL `Найти()` по типу+имени |

## Migration Plan

1. Реализовать BSL-функции и HTTP handlers в CFE
2. Загрузить расширение в `build/ib`, db-update
3. Обновить права роли, `patch-datamcp-vrd.ps1` при необходимости
4. Реализовать Java-слой, собрать JAR
5. curl-тесты трёх endpoint'ов
6. MCP end-to-end: `metadata`, `find_objects("номенклатур")`, `describe_object("Catalog.Номенклатура")`

Rollback: откатить CFE к версии skeleton (только ping); Java tools не регистрировать.

## Open Questions

_(нет — решения зафиксированы в explore)_
