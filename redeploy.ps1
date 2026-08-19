# redeploy.ps1 - restart Earthworm main frontend + backend (run as Administrator)
# Steps:
#   1. Stop old processes occupying ports 3000/8000
#   2. Pull latest code (no-op if already up to date)
#   3. Start prod service reusing existing backend jar + built .output
# Usage (Administrator PowerShell):
#   cd D:\earthworm-main
#   .\redeploy.ps1
# Or single line:
#   powershell -ExecutionPolicy Bypass -File D:\earthworm-main\redeploy.ps1

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "=== 1) Stop old processes on ports 3000/8000 ===" -ForegroundColor Cyan
$ports = @(3000, 8000)
$pids = @()
foreach ($p in $ports) {
    $conns = Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue
    foreach ($c in $conns) {
        $pidv = $c.OwningProcess
        if ($pidv -and ($pids -notcontains $pidv)) { $pids += $pidv }
    }
}
if ($pids.Count -gt 0) {
    Write-Host "About to stop PID(s): $($pids -join ', ')"
    try {
        Stop-Process -Id $pids -Force -ErrorAction Stop
        Write-Host "Stopped old processes, waiting for ports to free..." -ForegroundColor Green
    } catch {
        Write-Host "Failed to stop (Administrator required): $_" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "No process occupying the ports, nothing to stop"
}
Start-Sleep -Seconds 3

Write-Host "=== 2) Pull latest code (no-op if already current) ===" -ForegroundColor Cyan
git pull origin main 2>&1 | Select-Object -Last 3

Write-Host "=== 3) Start prod service (backend jar + frontend .output) ===" -ForegroundColor Cyan
& "$root\prod-start.ps1"
