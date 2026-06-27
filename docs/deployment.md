# Deployment: DataMcp extension

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

Assign the role to the HTTP API user (`datamcp` in test setup).

## Compatibility notes

- Extension `ConfigurationExtensionCompatibilityMode` must not exceed host config `CompatibilityMode` (UT 11.4 uses `Version8_3_10`).
- Borrowed language `Русский` must reference host UUID in `ExtendedConfigurationObject`.

## Assign role

Assign role `DataMcpReadOnly` to the HTTP service user, or use a user with sufficient read rights.
