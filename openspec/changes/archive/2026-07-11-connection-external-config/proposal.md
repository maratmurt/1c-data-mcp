## Why

Сейчас в Docker-образе URL подключений и полный список `datamcp.connections` запечены в `application-docker.yml`. Чтобы сменить порты, хосты или набор баз 1С, нужно править YAML в репозитории и пересобирать образ. Credentials уже вынесены в переменные окружения — топологию подключений тоже нужно вынести наружу.

## What Changes

- Поддержка внешнего YAML-конфига, монтируемого в runtime (volume), который перекрывает запечённые настройки подключений
- Шаблон `docker/datamcp-local.yml.example` для копирования; рабочий `datamcp-local.yml` — в gitignore
- Обновление `docker-compose.yml` и `.cursor/mcp.json.docker`: mount внешнего конфига и `SPRING_CONFIG_ADDITIONAL_LOCATION`
- Обновление `docker/README.md`: новый workflow без rebuild при смене URL/подключений
- `application-docker.yml` в образе остаётся fallback-дефолтами, если внешний файл отсутствует (префикс `optional:`)

## Capabilities

### New Capabilities

_(нет)_

### Modified Capabilities

- `connection-config`: требование на override через внешний YAML (Spring Boot additional config location)
- `docker-deployment`: вместо baked-only URLs — volume-mounted external config; документация для Cursor и compose

## Impact

- **Docker-артефакты**: `docker-compose.yml`, `docker/README.md`, `.cursor/mcp.json.docker`, новый `datamcp-local.yml.example`
- **Gitignore**: `docker/datamcp-local.yml`
- **Без изменений Java-кода** — существующий `@ConfigurationProperties` работает с внешним конфигом Spring
- **Spec deltas**: `connection-config`, `docker-deployment`
- **Non-breaking**: без внешнего файла поведение не меняется (дефолты из `application-docker.yml`)
