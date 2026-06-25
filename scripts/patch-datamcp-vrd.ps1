# Patches default.vrd after Configurator republish to enable DataMcp HTTP service.
param(
    [string]$VrdPath = "C:\Apache24\htdocs\1c-http-service\default.vrd"
)

$serviceBlock = @'
	<httpServices publishByDefault="true" publishExtensionsByDefault="true">
		<service name="DataMcp" rootUrl="datamcp/v1" enable="true"
				reuseSessions="autouse" sessionMaxAge="20"/>
	</httpServices>
'@

if (-not (Test-Path $VrdPath)) {
    Write-Error "default.vrd not found: $VrdPath"
    exit 1
}

$content = Get-Content -Raw -Encoding UTF8 $VrdPath
$pattern = '<httpServices[^>]*(?:/>|>[\s\S]*?</httpServices>)'
$replacement = $serviceBlock.TrimEnd()

if ($content -notmatch '<httpServices') {
    Write-Error "No <httpServices> block in $VrdPath"
    exit 1
}

$newContent = [regex]::Replace($content, $pattern, $replacement, 1)
if ($newContent -eq $content) {
    Write-Host "Already patched: $VrdPath"
    exit 0
}

Set-Content -Path $VrdPath -Value $newContent -Encoding UTF8
Write-Host "Patched: $VrdPath"
Write-Host "Restart Apache as Administrator: net stop Apache2.4 && net start Apache2.4"
