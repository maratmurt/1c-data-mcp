---
name: connect-1c
description: >
  Этот скилл MUST быть вызван когда нужно подключить информационную базу или проект 1С к MCP-серверу 1c-data:
  установить расширение DataMcp либо HTTP-сервис в основную конфигурацию, прописать подключение в docker/datamcp-local.yml и опубликовать базу в Apache.
  SHOULD также вызывать, когда пользователь говорит «подключи базу», «добавь подключение», «опубликуй для MCP», «настрой datamcp-local.yml».
  Do NOT использовать для выборки данных из уже подключённой базы — для этого MCP 1c-data (list_connections, execute_query).
argument-hint: "[database]"
---

# Подключение базы 1С к MCP 1c-data

Цель: `GET http://localhost:{Port}/{AppName}/hs/datamcp/v1/ping` отвечает `{"status":"ok"}`, а имя подключения видно в `list_connections` с `reachable: true`.

Исходники API: `src/cfe/DataMcp`. Корневой URL сервиса — `datamcp` (один сегмент). Шаблоны: `v1/ping`, `v1/metadata`, `v1/objects/search`, `v1/objects/{type}/{name}`, `v1/query`. Полный путь публикации: `/{AppName}/hs/datamcp/v1/...`.

Скрипты платформы — из этого репозитория: `.cursor/skills/<имя>/scripts/`. Параметры базы и `v8path` бери из `.v8-project.json` целевого проекта (или из явного пути, который дал пользователь). Полную загрузку основной конфигурации не делай.

## 1. Расширение или HTTP-сервис в основной конфигурации

Прочитай `DefaultRunMode` в `Configuration.xml` целевой конфигурации.

| `DefaultRunMode` | Куда ставить API |
|---|---|
| `ManagedApplication` | Расширение `DataMcp` |
| `OrdinaryApplication` | HTTP-сервис, общий модуль и роль в основной конфигурации |

`Auto` трактуй как управляемое, если в конфигурации есть управляемое приложение. Если загрузка расширения невозможна (режим совместимости ниже `Version8_3_10`, расширения не поддерживаются) — ставь объекты в основную конфигурацию, как для обычного приложения.

Пользователю 1С, под которым ходит MCP (`ONEC_USER`), назначь роль `DataMcpReadOnly`.

### Управляемое приложение — расширение

`ConfigurationExtensionCompatibilityMode` расширения — `Version8_3_10`. Он не должен быть новее `CompatibilityMode` основной конфигурации.

Перед загрузкой поправь заимствованный язык: в `src/cfe/DataMcp/Languages/Русский.xml` атрибут `ExtendedConfigurationObject` должен совпадать с UUID языка `Русский` целевой конфигурации. Не коммить чужой UUID в этот репозиторий — правь копию каталога расширения или верни файл после загрузки.

```powershell
powershell.exe -NoProfile -File .cursor/skills/db-load-xml/scripts/db-load-xml.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" -Password "<password>" `
  -ConfigDir "src/cfe/DataMcp" -Extension DataMcp -Mode Full

powershell.exe -NoProfile -File .cursor/skills/db-update/scripts/db-update.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" -Password "<password>" `
  -Extension DataMcp
```

Для серверной базы вместо `-InfoBasePath` передай `-InfoBaseServer` и `-InfoBaseRef`.

### Обычное приложение — объекты в основной конфигурации

Скопируй в выгрузку целевой конфигурации (`src/cf` того проекта) только:

- `HTTPServices/DataMcp.xml` и `HTTPServices/DataMcp/`
- `CommonModules/DataMcp_Общий.xml` и `CommonModules/DataMcp_Общий/`
- `Roles/DataMcpReadOnly.xml` и `Roles/DataMcpReadOnly/`

Не копируй `Configuration.xml` расширения и `Languages/` (язык там заимствованный).

Зарегистрируй объекты в `Configuration.xml` целевой конфигурации:

```powershell
powershell.exe -NoProfile -File .cursor/skills/cf-edit/scripts/cf-edit.ps1 `
  -ConfigPath "<src/cf>" -Operation add-childObject `
  -Value "CommonModule.DataMcp_Общий ;; HTTPService.DataMcp ;; Role.DataMcpReadOnly"
```

Если в целевом репозитории `src/cf/**` в `.gitignore`, открой исключения на эти три объекта (каталог типа, XML и каталог объекта) после строки `src/cf/**`.

Загрузи только эти файлы (`-Mode Partial`) и обнови основную конфигурацию БД **без** `-Extension`. Перед полной заменой конфигурации остановись и спроси пользователя.

`RootURL` оставь `datamcp`. Версию в шаблонах URL не переноси в `RootURL`.

## 2. `docker/datamcp-local.yml`

Файл локальный и в git не входит. Если его нет — скопируй `docker/datamcp-local.yml.example`.

Имя подключения — короткое (`ut`, `bp`). `url` — публикация Apache, как её видит контейнер: хост `host.docker.internal`, не `localhost`. Порт и `{AppName}` совпадают с шагом 3.

```yaml
datamcp:
  default-connection: ut
  connections:
    - name: ut
      url: http://host.docker.internal:8081/ut
      username: ${ONEC_USER:}
      password: ${ONEC_PASSWORD:}
```

Учётные данные — в `docker/.env` (`ONEC_USER`, `ONEC_PASSWORD`), не в YAML. Файл `.env` не коммить.

После правки YAML перезапусти контейнер из каталога `docker/`:

```powershell
docker compose restart mcp-server-http
```

Новое подключение подхватывается без пересборки образа.

## 3. Apache

Публикация через скилл `web-publish`. `-AppName` — сегмент URL (нижний регистр), `-Port` по умолчанию `8081`, `-ApachePath` — из `webPath` в `.v8-project.json` или `C:\Apache24`.

```powershell
powershell.exe -NoProfile -File .cursor/skills/web-publish/scripts/web-publish.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" -Password "<password>" `
  -AppName ut -Port 8081 -ApachePath "C:\Apache24"
```

Скрипт пишет `default.vrd` с `publishByDefault="true"` и `publishExtensionsByDefault="true"` и добавляет в `httpd.conf` блок `Alias` + `SetHandler 1c-application`. Повторный вызов с тем же `-AppName` обновляет публикацию. Вторая база — другой `-AppName` на том же порту и вторая запись в `datamcp-local.yml`.

Проверка с хоста (до контейнера):

```powershell
curl.exe -u $env:ONEC_USER:$env:ONEC_PASSWORD http://localhost:8081/ut/hs/datamcp/v1/ping
```

Ожидается HTTP 200 и `"status": "ok"`. Затем `list_connections`: у нового имени `reachable: true`.

## Если ping не открывается

| Симптом | Что проверить |
|---|---|
| 404 на `/hs/datamcp/...` | Расширение обновлено в БД; в `default.vrd` есть `httpServices` с `publishExtensionsByDefault="true"` (для расширения) |
| 401 | Пользователь из `ONEC_USER` / `ONEC_PASSWORD` и роль `DataMcpReadOnly` |
| Контейнер `reachable: false`, curl с хоста живой | В YAML `host.docker.internal`, не `localhost`; контейнер перезапущен |
| Загрузка расширения падает на языке | UUID в `Languages/Русский.xml` |
