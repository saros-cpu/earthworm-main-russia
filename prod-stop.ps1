$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$runtimeDir = if ($env:EARTHWORM_RUNTIME_DIR) { $env:EARTHWORM_RUNTIME_DIR } else { Join-Path $root "runtime" }
. (Join-Path $root "scripts\prod-process-control.ps1")

$backendStopped = Stop-ManagedProcess $runtimeDir "backend"
$frontendStopped = Stop-ManagedProcess $runtimeDir "frontend"

if (-not $backendStopped) {
    Write-Host "No managed backend process was stopped."
}
if (-not $frontendStopped) {
    Write-Host "No managed frontend process was stopped."
}

Write-Host ""
Write-Host "Managed production processes stopped. Unknown port owners were left untouched." -ForegroundColor Green
