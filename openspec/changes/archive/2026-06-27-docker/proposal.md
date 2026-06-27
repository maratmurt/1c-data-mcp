## Why

Этапы 1–4 MVP завершены: MCP-сервер работает локально через `java -jar` и STDIO. Для воспроизводимого dev setup на машинах команды нужна контейнеризация Java-адаптера — без ручной установки JDK и сборки JAR. Это финальный этап MVP roadmap.

## What Changes

- Добавить `docker/Dockerfile` (multi-stage: Gradle build → JRE runtime)
- Добавить `docker/docker-compose.yml` для локального запуска с env vars (`ONEC_USER`, `ONEC_PASSWORD`)
- Добавить baked `application-docker.yml` с URL через `host.docker.internal` (профиль `docker`)
- Добавить `docker/README.md`: build, compose, интеграция с Cursor (`docker run -i`)
- Добавить пример `.cursor/mcp.json.docker` (не перезаписывать рабочий `mcp.json`)
- Обновить `docs/mvp-roadmap.md` (этап 5 ✅ после реализации)
- Cross-link из `server/README.md` и `docs/deployment.md` на docker docs

**Не меняется:** расширение `DataMcp`, 1С в Docker, STREAMABLE HTTP transport, CI/registry publish, mount config.

## Capabilities

### New Capabilities

- `docker-deployment`: Dockerfile, docker-compose, baked docker profile, dev documentation, Cursor STDIO integration

### Modified Capabilities

- `mcp-stdio-server`: сценарий запуска MCP-сервера в Docker-контейнере через STDIO transport

## Impact

- **Java**: `server/src/main/resources/application-docker.yml` (новый профиль)
- **Docker**: `docker/` — Dockerfile, compose, `.env.example`, README
- **Документация**: `docker/README.md`, `server/README.md`, `docs/deployment.md`, `docs/mvp-roadmap.md`
- **Cursor**: пример `mcp.json.docker` (опциональный dev workflow)
- **Не затрагивается**: `src/cfe/DataMcp/`, Spring AI BOM, MCP tools API, 1С Apache publication
