## 1. Шаблон внешнего конфига

- [x] 1.1 Создать `docker/datamcp-local.yml.example` с `datamcp.default-connection`, `datamcp.connections` (ut, ut-copy), URL через `host.docker.internal` и плейсхолдерами `${ONEC_USER}` / `${ONEC_PASSWORD}`
- [x] 1.2 Добавить `docker/datamcp-local.yml` в `.gitignore`

## 2. Docker compose и docker run

- [x] 2.1 Обновить `docker/docker-compose.yml`: mount `./datamcp-local.yml:/config/application.yml:ro`, задать `SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/`
- [x] 2.2 Обновить `.cursor/mcp.json.docker`: добавить `-v` для внешнего конфига и `-e SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/config/` (документировать placeholder абсолютного пути)

## 3. Документация

- [x] 3.1 Обновить `docker/README.md`: заменить «правь application-docker.yml и rebuild» на workflow с внешним конфигом (`cp datamcp-local.yml.example datamcp-local.yml`, правка URL, перезапуск контейнера)
- [x] 3.2 Добавить в таблицу конфигурации `SPRING_CONFIG_ADDITIONAL_LOCATION` и путь к внешнему конфигу
- [x] 3.3 Добавить примеры PowerShell и bash для `docker run -i` с volume mount

## 4. Проверка

- [x] 4.1 Smoke test: контейнер с внешним конфигом на кастомный URL — `list_connections` отражает mounted config без rebuild образа
- [x] 4.2 Smoke test: контейнер без `datamcp-local.yml` (префикс optional) — baked defaults docker profile работают
