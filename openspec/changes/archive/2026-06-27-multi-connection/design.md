## Context

Этапы skeleton–query реализовали multi-connection каркас на Java:

- `DataMcpProperties.connections[]` + `default-connection`
- `WebClientConfig` — `Map<String, WebClient>` при старте
- `ConnectionService.resolveConnection()` — explicit name или default
- Все MCP tools принимают опциональный `connection`
- `MetadataService` — кэш flat-index в `ConcurrentHashMap` keyed by connection name
- `AuditLogger` — connection в audit log

Фактически в `application.yml` и integration-тестах используется одна база `ut`. Спеки описывают single-connection сценарии. Этап 4 MVP — формально завершить операционную модель «один MCP-сервер → несколько баз» перед Docker.

Расширение `DataMcp` не меняется: каждая публикация обслуживает одну ИБ.

## Goals / Non-Goals

**Goals:**

- Зафиксировать требования multi-connection в delta specs
- Unit-тесты `ConnectionService` без 1С
- Integration-тесты с `ut` (reachable) + `unreachable` (мертвый порт) через `application-test.yml`
- Тест изоляции metadata cache между connections
- Документация: пример второй публикации той же ИБ для smoke
- Обновить roadmap (этап 4 ✅)

**Non-Goals:**

- Изменения BSL/CFE (`src/cfe/DataMcp/`)
- Per-connection query limits (`datamcp.query` per connection)
- Hot-reload connections без перезапуска MCP-сервера
- MockWebServer / WireMock (можно добавить позже для CI)
- Docker, HTTP MCP transport (этап 5)
- «Session connection» — запоминание последнего использованного connection (остаётся default-only)

## Decisions

### 1. Hardening, не переписывание

**Решение:** Не вводить отдельный `ConnectionResolver` — `ConnectionService` уже выполняет эту роль. Change ограничен тестами, спеками и документацией; код меняется только при обнаружении пробелов.

**Альтернатива:** рефакторинг в `ConnectionResolver` + `ConnectionRegistry` — отвергнута: YAGNI, текущая структура достаточна.

### 2. Тестовая конфигурация: `application-test.yml`

**Решение:** `src/test/resources/application-test.yml`:

```yaml
datamcp:
  default-connection: ut
  connections:
    - name: ut
      url: http://localhost:8081/datamcp
      username: ${ONEC_USER:}
      password: ${ONEC_PASSWORD:}
    - name: unreachable
      url: http://localhost:59999/datamcp
      username: test
      password: test
```

Integration-тесты активируют профиль `test` (`@ActiveProfiles("test")`).

**Альтернатива:** `@TestPropertySource` inline — отвергнута: yaml проще поддерживать и документировать.

### 3. Пирамида тестов

| Уровень | Класс | Зависимость от 1С |
|---------|-------|-------------------|
| Unit | `ConnectionServiceTest` | Нет |
| Integration | `ConnectionIntegrationTest` | Частично (`ut` reachable) |
| Integration | расширение `MetadataIntegrationTest` | Да (cache isolation) |
| Smoke | вторая публикация `ut-copy` :9090 | Да (ручной) |
| MCP e2e | Cursor `list_connections` | Да (ручной) |

**Решение:** `ConnectionServiceTest` — всегда в CI. Integration с живой 1С — опционально через `@EnabledIfEnvironmentVariable(named = "ONEC_INTEGRATION", matches = "true")`, чтобы `gradle test` без Apache не падал в CI.

**Альтернатива:** всегда требовать 1С (как сейчас) — отвергнута для CI-устойчивости.

### 4. Smoke: две публикации одной ИБ

**Решение:** Для полного e2e без второй конфигурации — опубликовать `build/ib` дважды:

```
:8081/datamcp   → connection: ut
:9090/datamcp2  → connection: ut-copy
```

Одни данные, разные URL — достаточно для проверки routing и per-connection cache.

**Альтернатива:** вторая ИБ (БП, ERP) — не обязательна для MVP; документируем как optional smoke.

### 5. Поведение при unknown connection

**Решение:** `resolveConnection("unknown")` → `IllegalArgumentException` с текстом `Connection not configured: unknown`. MCP tool возвращает ошибку клиенту, HTTP к 1С не выполняется.

Reachability (`reachable: false`) отделена от routing: несуществующее имя — ошибка конфигурации; существующее, но недоступное — `list_connections` показывает `reachable: false`, tools при вызове получают HTTP error.

### 6. Default connection не «залипает»

**Решение:** Параметр `connection` в каждом tool call независим. Отсутствие `connection` → всегда `default-connection`, не «последний использованный». Зафиксировать тестом.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| Integration-тесты падают без Apache | `@EnabledIfEnvironmentVariable(ONEC_INTEGRATION)` |
| Агент путает базы (find на ut, query на default) | Документация + `list_connections` как первый вызов; тест default vs explicit |
| Две публикации одной ИБ — лишняя инфра для smoke | Опциональный шаг в tasks; unit + unreachable покрывают routing |
| Кэш metadata растёт с числом connections | Приемлемо для MVP (2–5 баз); TTL уже есть |

## Migration Plan

1. Добавить `application-test.yml` и тесты
2. Обновить спеки (archive change → main specs)
3. Обновить `server/README.md`, `docs/deployment.md`
4. Отметить этап 4 в `docs/mvp-roadmap.md`
5. Опциональный smoke: вторая публикация + MCP e2e в Cursor

Rollback: revert тестов и спеков; runtime-поведение не меняется.

## Open Questions

- Включать ли `@EnabledIfEnvironmentVariable` в этом change или оставить текущее поведение (все integration-тесты требуют 1С)?
  - **Рекомендация:** включить для новых `ConnectionIntegrationTest`; существующие `MetadataIntegrationTest` / `QueryIntegrationTest` не трогать в этом change.
