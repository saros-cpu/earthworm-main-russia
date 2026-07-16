# register-earthworm-task.ps1 - Register Earthworm auto-start on boot
# Usage (as Administrator):
#   powershell -ExecutionPolicy Bypass -File register-earthworm-task.ps1        # 注册
#   powershell -ExecutionPolicy Bypass -File register-earthworm-task.ps1 -Unregister  # 卸载

param([switch]$Unregister)

$ErrorActionPreference = "Stop"
$taskName = "Earthworm-Platform"
$scriptPath = "D:\earthworm-main\start-earthworm.ps1"

# ─── 卸载 ───
if ($Unregister) {
    $existing = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
    if ($existing) {
        Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
        Write-Host "Task '$taskName' unregistered."
    } else {
        Write-Host "Task '$taskName' does not exist."
    }

    # 停掉当前运行的进程
    & "D:\earthworm-main\prod-stop.ps1"
    return
}

# ─── 前置检查 ───
if (-not (Test-Path $scriptPath)) {
    Write-Host "ERROR: $scriptPath not found" -ForegroundColor Red
    exit 1
}

Write-Host "=== Register Earthworm Auto-Start ==="

# ─── 删除已有任务 ───
$existing = Get-ScheduledTask -TaskName $taskName -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Removing existing task '$taskName' ..."
    Unregister-ScheduledTask -TaskName $taskName -Confirm:$false
}

# ─── 创建任务 ───
$action = New-ScheduledTaskAction `
    -Execute "powershell" `
    -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$scriptPath`"" `
    -WorkingDirectory "D:\earthworm-main"

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

Write-Host ""
Write-Host "Task '$taskName' registered successfully."
Write-Host "Earthworm will auto-start on system boot."
Write-Host ""
Write-Host "Manual start: Start-ScheduledTask -TaskName '$taskName'"
Write-Host "Manual stop:  .\prod-stop.ps1"
Write-Host "Unregister:   powershell -File register-earthworm-task.ps1 -Unregister"
