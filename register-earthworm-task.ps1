# register-earthworm-task.ps1 - Register Earthworm auto-start on boot
# Usage (as Administrator):
#   powershell -ExecutionPolicy Bypass -File register-earthworm-task.ps1        # 注册
#   powershell -ExecutionPolicy Bypass -File register-earthworm-task.ps1 -Unregister  # 卸载

param([switch]$Unregister)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$taskName = "Earthworm-Platform"
$watchdogTaskName = "Earthworm-Watchdog"
$scriptPath = Join-Path $root "start-earthworm.ps1"

# --- 卸载 ---
if ($Unregister) {
    $existing = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    if ($existing) {
        Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
        Write-Host "Task '$taskName' unregistered."
    } else {
        Write-Host "Task '$taskName' does not exist."
    }

    & (Join-Path $root "prod-stop.ps1")

    $watchdogExisting = Get-ScheduledTask -TaskName $watchdogTaskName -ErrorAction SilentlyContinue
    if ($watchdogExisting) {
        Unregister-ScheduledTask -TaskName $watchdogTaskName -Confirm:$false
        Write-Host "Task '$watchdogTaskName' unregistered."
    }
    return
}

# --- 前置检查 ---
if (-not (Test-Path $scriptPath)) {
    Write-Host "ERROR: $scriptPath not found" -ForegroundColor Red
    exit 1
}

Write-Host "=== Register Earthworm Auto-Start ==="

# --- 删除已有任务 ---
$existing = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Removing existing task '$taskName' ..."
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
}

# --- 创建启动任务 ---
$action = New-ScheduledTaskAction `
    -Execute "powershell" `
    -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$scriptPath`"" `
    -WorkingDirectory $root

$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew

Register-ScheduledTask `
    -TaskName $taskName `
    -Action $action `
    -Trigger $trigger `
    -Principal $principal `
    -Settings $settings `
    -Description "Earthworm Russian Learning Platform - production backend and frontend"

# --- 注册 Watchdog ---
$watchdogScript = Join-Path $root "scripts\prod-watchdog.ps1"
if (Test-Path $watchdogScript) {
    $watchdogAction = New-ScheduledTaskAction `
        -Execute "powershell" `
        -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$watchdogScript`"" `
        -WorkingDirectory $root
    $watchdogTrigger = New-ScheduledTaskTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Minutes 5) -RepetitionDuration (New-TimeSpan -Days 30)
    $watchdogSettings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable -MultipleInstances IgnoreNew
    Register-ScheduledTask -TaskName $watchdogTaskName -Action $watchdogAction -Trigger $watchdogTrigger -Principal $principal -Settings $watchdogSettings -Description "Earthworm health check every 5 minutes"
    Write-Host ""
    Write-Host "Task '$watchdogTaskName' registered (runs every 5 minutes)."
} else {
    Write-Host ""
    Write-Host "WARNING: $watchdogScript not found - watchdog not registered." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Task '$taskName' registered successfully."
Write-Host "Earthworm will auto-start on system boot."
Write-Host ""
Write-Host "Manual start: Start-ScheduledTask -TaskName '$taskName'"
Write-Host "Manual stop:  .\prod-stop.ps1"
Write-Host "Unregister:   powershell -File register-earthworm-task.ps1 -Unregister"
