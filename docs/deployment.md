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
  -V8Path $v8 -InfoBasePath $ib -UserName "<user>" -AppName ut -Port 80 -ApachePath "C:\Apache24"
```

If Apache is not installed, install Apache 2.4 manually or fix `tools/apache24` per `web-publish` skill.

## 4. Verify ping

```bash
curl -u "user:password" http://localhost/ut/hs/datamcp/v1/ping
```

Expected response:

```json
{
  "status": "ok",
  "configuration": "УправлениеТорговлей",
  "version": "11.4.2.132"
}
```

## Compatibility notes

- Extension `ConfigurationExtensionCompatibilityMode` must not exceed host config `CompatibilityMode` (UT 11.4 uses `Version8_3_10`).
- Borrowed language `Русский` must reference host UUID in `ExtendedConfigurationObject`.

## Assign role

Assign role `DataMcpReadOnly` to the HTTP service user, or use a user with sufficient read rights.
