## Why

Этапы skeleton–query заложили каркас multi-connection (`ConnectionService`, параметр `connection` во всех MCP tools, кэш per connection), но фактически протестирована только одна база `ut`. Без формальной валидации routing агент не может надёжно работать с несколькими 1С-базами (dev/prod, разные конфигурации) — а это операционная модель MVP перед Docker-развёртыванием.

## What Changes

- Зафиксировать и протестировать multi-connection runtime: несколько connections в конфиге, `default-connection`, routing по имени
- Добавить unit-тесты `ConnectionService` (resolve, unknown name, default fallback)
- Добавить integration-тесты: `list_connections` с reachable + unreachable; изоляция metadata cache между connections
- Добавить `application-test.yml` с двумя connections (`ut` + `unreachable`) для тестового профиля
- Документировать пример второй публикации той же ИБ (`ut-copy` на другом порту) для полного e2e smoke
- Обновить `server/README.md` и `docs/mvp-roadmap.md` (этап 4)
- Опционально: `@EnabledIfEnvironmentVariable` для integration-тестов, требующих живую 1С

**Не меняется:** расширение `DataMcp` (1С не знает про multi-connection), Docker (этап 5), per-connection query limits.

## Capabilities

### New Capabilities

_(нет — новых подсистем не вводим)_

### Modified Capabilities

- `connection-config`: сценарии multiple connections, отклонение неизвестного имени connection
- `mcp-stdio-server`: сценарии `list_connections` с несколькими базами; ошибка при несуществующем `connection` в tools
- `metadata-cache`: явный сценарий изоляции кэша между connections

## Impact

- **Java**: `server/` — тесты (`ConnectionServiceTest`, `ConnectionIntegrationTest`), расширение `MetadataIntegrationTest`, `application-test.yml`, возможно мелкие правки `ConnectionService` при обнаружении пробелов
- **Документация**: `server/README.md`, `docs/deployment.md` (вторая публикация), `docs/mvp-roadmap.md`
- **Не затрагивается**: `src/cfe/DataMcp/`, `src/cf/`, Docker (этап 5)
- **Инфраструктура**: для полного smoke — вторая web-публикация `build/ib` на другом порту; для CI — unit-тесты без 1С + unreachable connection без второй публикации
