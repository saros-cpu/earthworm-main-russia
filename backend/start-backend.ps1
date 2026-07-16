# start-backend.ps1 - Start Spring Boot backend silently (no window)
# Usage: powershell -ExecutionPolicy Bypass -File start-backend.ps1

$backendDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$logFile = Join-Path $backendDir "backend.log"
$mvn = "C:\Users\User\apps\apache-maven-3.9.15\bin\mvn.cmd"

$old = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($old) {
    Write-Host "Stopping existing backend (PID: $($old.Id -join ', ')) ..."
    $old | Stop-Process -Force
    Start-Sleep -Seconds 2
}

Write-Host "Starting backend ..."
Start-Process -FilePath "cmd.exe" -ArgumentList "/c `"`"$mvn`" spring-boot:run -DskipTests -f `"$backendDir\pom.xml`" > `"$logFile`" 2>&1`"" -WindowStyle Hidden

Start-Sleep -Seconds 3
$proc = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($proc) {
    Write-Host "Backend started (PID: $($proc.Id -join ', '))"
    Write-Host "Log: $logFile"
} else {
    Write-Host "Backend may still be starting, check log: $logFile"
}
