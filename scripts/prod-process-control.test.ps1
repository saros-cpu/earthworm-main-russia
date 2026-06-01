$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "prod-process-control.ps1")

$testRuntime = Join-Path ([System.IO.Path]::GetTempPath()) ("earthworm-process-control-" + [Guid]::NewGuid())
$powershellPath = (Get-Process -Id $PID).Path
$owned = $null
$unrelated = $null

function Start-TestProcess {
    return Start-Process `
        -FilePath $powershellPath `
        -ArgumentList "-NoProfile", "-Command", "Start-Sleep -Seconds 60" `
        -PassThru `
        -WindowStyle Hidden
}

try {
    $owned = Start-TestProcess
    Save-ManagedProcessRecord $testRuntime "frontend" $owned
    if (-not (Stop-ManagedProcess $testRuntime "frontend")) {
        throw "Managed process was not stopped."
    }
    if (-not $owned.WaitForExit(5000)) {
        throw "Managed process did not exit."
    }

    $unrelated = Start-TestProcess
    Save-ManagedProcessRecord $testRuntime "backend" $unrelated
    $recordPath = Get-ManagedProcessRecordPath $testRuntime "backend"
    $record = Get-Content -Raw -Encoding UTF8 -Path $recordPath | ConvertFrom-Json
    $record.StartedAtUtcTicks = 0
    $record | ConvertTo-Json | Set-Content -Encoding UTF8 -Path $recordPath

    if (Stop-ManagedProcess $testRuntime "backend") {
        throw "Stale process record unexpectedly stopped an unrelated process."
    }
    $unrelated.Refresh()
    if ($unrelated.HasExited) {
        throw "Stale process record terminated an unrelated process."
    }

    function Get-NetTCPConnection {
        [CmdletBinding()]
        param([string]$State, [int]$LocalPort)
        return [PSCustomObject]@{ OwningProcess = $PID }
    }
    try {
        Assert-PortAvailable 3000 "Test listener"
        throw "Occupied port was incorrectly treated as available."
    } catch {
        if ($_.Exception.Message -notlike "*occupied by an unmanaged process*") {
            throw
        }
    }

    Write-Host "Production process ownership checks passed."
} finally {
    if ($owned -and -not $owned.HasExited) {
        Stop-Process -Id $owned.Id -Force -ErrorAction SilentlyContinue
    }
    if ($unrelated -and -not $unrelated.HasExited) {
        Stop-Process -Id $unrelated.Id -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $testRuntime) {
        Remove-Item -LiteralPath $testRuntime -Force -ErrorAction SilentlyContinue
    }
}
