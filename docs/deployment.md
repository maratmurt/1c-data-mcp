# Deployment: DataMcp extension

For containerized MCP server setup (Java side only), see [docker/README.md](../docker/README.md).

## Prerequisites

- 1C:Enterprise 8.3 platform (Designer)
- Apache HTTP Server for web publication (or existing web server)
- UT test base at `build/ib`

## 1. Load extension

```powershell
$v8 = "C:\Program Files\1cv8\8.3.27.1644\bin"
$ib = "c:\Users\marat\PROJECTS\1c-data-mcp\build\ib"
$cfg = "c:\Users\marat\PROJECTS\1c-data-mcp\src\cfe\DataMcp"

powershell.exe -NoProfile -File .cursor/skills/db-load-xml/scripts/db-load-xml.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" `
  -ConfigDir $cfg -Extension DataMcp -Mode Full
```

## 2. Update database

```powershell
powershell.exe -NoProfile -File .cursor/skills/db-update/scripts/db-update.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" -Extension DataMcp
```

## 3. Web publish

```powershell
powershell.exe -NoProfile -File .cursor/skills/web-publish/scripts/web-publish.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" -AppName datamcp -ApachePath "C:\Apache24"
```

Publication URL example: `http://localhost:8081/datamcp`

HTTP service root URL in metadata: `datamcp` (templates under `v1/...`).

## 4. Verify endpoints

```bash
curl -u datamcp:1 http://localhost:8081/datamcp/hs/datamcp/v1/ping
curl -u datamcp:1 http://localhost:8081/datamcp/hs/datamcp/v1/metadata
curl -u datamcp:1 "http://localhost:8081/datamcp/hs/datamcp/v1/objects/search?q=номенклатур&limit=5"
curl -u datamcp:1 "http://localhost:8081/datamcp/hs/datamcp/v1/objects/Catalog/Номенклатура"
curl -u datamcp:1 -X POST -H "Content-Type: application/json; charset=utf-8" \
  -d '{"query":"ВЫБРАТЬ ПЕРВЫЕ 10\n    Номенклатура.Наименование\nИЗ\n    Справочник.Номенклатура КАК Номенклатура"}' \
  http://localhost:8081/datamcp/hs/datamcp/v1/query
```

Expected ping response:

```json
{
  "status": "ok",
  "configuration": "УправлениеТорговлей",
  "version": "11.4.2.132"
}
```

Metadata summary includes `counts` for `Catalog`, `Document`, `Enum`, `InformationRegister`, `AccumulationRegister`.

## Role rights

Role `DataMcpReadOnly` must grant GET on URL templates:

- `ping`
- `metadata`
- `objects_search`
- `objects_type_name`
- `query` (POST)

## Query security

- Java `QueryGuard`: max length, must start with `ВЫБРАТЬ`, forbidden tokens
- 1C `SecuritySvc`: authoritative validation before `Запрос.Выполнить()`
- Execution in privileged mode after validation
- Audit: SHA-256 hash of query text (no full text in logs)

Assign the role to the HTTP API user (`datamcp` in test setup).

## Multi-connection smoke (optional)

## Multi-connection example (second infobase)

Publish a second infobase on the same Apache listener under a different path:

```powershell
$v8 = "C:\Program Files\1cv8\8.3.27.1644\bin"
$ib = "C:\Users\marat\PROJECTS\_test\1c-study-reports\build\ib"

powershell.exe -NoProfile -File .cursor/skills/web-publish/scripts/web-publish.ps1 `
  -V8Path $v8 -InfoBasePath $ib -UserName "datamcp" `
  -AppName reports -ApachePath "C:\Apache24"
```

Publication URL: `http://localhost:8081/reports`

For configurations that do not support extensions, copy `DataMcp` HTTP service objects (HTTP service, common module, role) into the main configuration instead of loading the CFE extension.

Add to local MCP config (`application.yml` or `docker/datamcp-local.yml`; do not commit credentials):

```yaml
datamcp:
  connections:
    - name: reports
      url: http://localhost:8081/reports
      username: ${ONEC_USER:}
      password: ${ONEC_PASSWORD:}
```

Manual MCP smoke in Cursor:

1. `list_connections` — `ut` and `reports` both `reachable: true`
2. `metadata` with `connection=reports`
3. `find_objects` with `connection=reports` and a query matching that configuration's metadata

## Compatibility notes

- Extension `ConfigurationExtensionCompatibilityMode` must not exceed host config `CompatibilityMode` (UT 11.4 uses `Version8_3_10`).
- Borrowed language `Русский` must reference host UUID in `ExtendedConfigurationObject`.
