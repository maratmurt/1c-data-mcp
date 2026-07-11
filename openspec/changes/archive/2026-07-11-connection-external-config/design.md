## Context

Docker deployment (change `2026-06-27-docker`) ввёл Spring profile `docker` с запечёнными URL в `application-docker.yml`. Credentials вынесены через `ONEC_USER` / `ONEC_PASSWORD`, но топология подключений (URL, имена, default connection) остаётся внутри образа. В archived design volume-mounted config был явно отложен.

MCP-сервер использует Spring Boot 3.4 `@ConfigurationProperties` (`DataMcpProperties`), привязку из YAML при старте. `WebClient` beans создаются один раз из конфига — hot reload не нужен.

## Goals / Non-Goals

**Goals:**

- Менять URL, имена подключений и `default-connection` без пересборки Docker-образа
- Один внешний YAML-файл, монтируемый в runtime (вариант B из explore-сессии)
- Запечённый `application-docker.yml` как fallback, если внешний файл отсутствует
- Example config, gitignored local file, обновлённые compose и Cursor integration
- Без изменений Java-кода

**Non-Goals:**

- Hot reload подключений в runtime (перезапуск контейнера — норма)
- Env vars на каждое подключение (`ONEC_UT_URL`) — отложено; внешний YAML покрывает этот кейс
- Production secrets management (Vault и т.п.)
- Смена настроек вне connections через внешний файл поддерживается Spring, но явно не документируется за пределами `datamcp.*`

## Decisions

### 1. Внешний конфиг через `SPRING_CONFIG_ADDITIONAL_LOCATION`

**Решение:** монтировать внешний YAML в `/config/application.yml` и задать:

```
SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/
```

Префикс `optional:` — старт без файла успешен, применяются дефолты из profile.

**Обоснование:** стандартный механизм Spring Boot; без кастомного парсера; тот же формат YAML; override корректно мержится (внешний побеждает для `datamcp.*`).

**Альтернативы:**

- Env placeholders на URL (`${ONEC_UT_URL}`) — проще для одного URL, не масштабируется на произвольный список connections
- JSON env var `CONNECTIONS_JSON` — нужен кастомный Java binding
- Bind mount всего `application.yml` вместо classpath config — теряется layering профилей

### 2. Расположение и именование файлов

**Решение:**

```
docker/
├── datamcp-local.yml.example   # шаблон в git
├── datamcp-local.yml           # gitignored, копия разработчика
```

Монтируется как `/config/application.yml:ro` внутри контейнера.

**Обоснование:** понятное именование; паттерн `.example` как у `.env.example`; локальный файл не попадает в git.

### 3. Credentials остаются в переменных окружения

**Решение:** внешний YAML ссылается на `${ONEC_USER:}` и `${ONEC_PASSWORD:}` — как в baked config. Секреты не хранятся в смонтированном файле.

### 4. Baked defaults сохраняются

**Решение:** `application-docker.yml` в образе без изменений. Внешний файл полностью перекрывает `datamcp.connections` и `datamcp.default-connection`, когда присутствует.

**Обоснование:** zero-config Docker работает для типичного dev setup (8081/9090). Power users настраивают через mount.

### 5. Cursor integration — абсолютный путь volume

**Решение:** `.cursor/mcp.json.docker` включает `-v` с документированным путём; разработчик подставляет абсолютный путь под свою машину.

**Обоснование:** Cursor `docker run` не гарантирует working directory; относительные `-v` на Windows ненадёжны.

### 6. Compose volume в определении сервиса

**Решение:** добавить volume и env в сервис `mcp-server` в `docker-compose.yml`:

```yaml
volumes:
  - ./datamcp-local.yml:/config/application.yml:ro
environment:
  SPRING_CONFIG_ADDITIONAL_LOCATION: optional:file:/config/
```

Перед первым запуском: `cp datamcp-local.yml.example datamcp-local.yml`.

## Risks / Trade-offs

| Риск | Митигация |
|------|-----------|
| Нет `datamcp-local.yml` при первом запуске | префикс `optional:` → baked defaults; README описывает шаг `cp` |
| Невалидный YAML во внешнем файле | Spring падает при старте с понятной ошибкой; smoke через compose |
| Проблемы с путями Windows в Cursor `-v` | документировать абсолютный путь (`C:/Users/...`); пример PowerShell в README |
| Внешний файл перекрывает весь список connections | документировать: partial override не поддерживается — нужен полный `datamcp.connections` |
| Устаревший конфиг после смены публикации 1С | перезапуск контейнера (приемлемо для dev MCP workflow) |

## Migration Plan

1. Добавить `datamcp-local.yml.example`, обновить `.gitignore`
2. Обновить `docker-compose.yml`, `docker/README.md`, `.cursor/mcp.json.docker`
3. Существующие пользователи без внешнего файла: ничего не делать, поведение прежнее
4. Кто правил `application-docker.yml` локально: перенести настройки в `datamcp-local.yml`, перестать rebuild ради URL

Rollback: убрать volume mount и env var; вернуться к baked-only workflow.

## Open Questions

- Нет — подход подтверждён в explore-сессии (вариант B).
