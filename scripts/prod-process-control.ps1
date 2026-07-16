function Get-ManagedProcessRecordPath {
    param(
        [Parameter(Mandatory = $true)][string]$RuntimeDirectory,
        [Parameter(Mandatory = $true)][string]$Name
    )

    return Join-Path $RuntimeDirectory "$Name-process.json"
}

function Save-ManagedProcessRecord {
    param(
        [Parameter(Mandatory = $true)][string]$RuntimeDirectory,
        [Parameter(Mandatory = $true)][string]$Name,
        [Parameter(Mandatory = $true)][System.Diagnostics.Process]$Process
    )

    if (-not (Test-Path $RuntimeDirectory)) {
        New-Item -ItemType Directory -Path $RuntimeDirectory | Out-Null
    }
    $record = [PSCustomObject]@{
        Pid = $Process.Id
        StartedAtUtcTicks = $Process.StartTime.ToUniversalTime().Ticks
        ProcessName = $Process.ProcessName
    }
    $record | ConvertTo-Json | Set-Content -Encoding UTF8 -Path (Get-ManagedProcessRecordPath $RuntimeDirectory $Name)
}

function Stop-ManagedProcess {
    param(
        [Parameter(Mandatory = $true)][string]$RuntimeDirectory,
        [Parameter(Mandatory = $true)][string]$Name
    )

    $recordPath = Get-ManagedProcessRecordPath $RuntimeDirectory $Name
    if (-not (Test-Path $recordPath)) {
        return $false
    }

    try {
        $record = Get-Content -Raw -Encoding UTF8 -Path $recordPath | ConvertFrom-Json
        $process = Get-Process -Id ([int]$record.Pid) -ErrorAction SilentlyContinue
        if (-not $process) {
            return $false
        }
        $sameInstance = $process.ProcessName -eq [string]$record.ProcessName `
            -and $process.StartTime.ToUniversalTime().Ticks -eq [long]$record.StartedAtUtcTicks
        if (-not $sameInstance) {
            Write-Warning "Skipped stale $Name process record; the PID now belongs to another process."
            return $false
        }
        Stop-Process -Id $process.Id -Force -ErrorAction Stop
        Write-Host "Stopped managed $Name PID $($process.Id)"
        return $true
    } finally {
        Remove-Item -LiteralPath $recordPath -Force -ErrorAction SilentlyContinue
    }
}

function Assert-PortAvailable {
    param(
        [Parameter(Mandatory = $true)][int]$Port,
        [Parameter(Mandatory = $true)][string]$Name
    )

    $listeners = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
    if (-not $listeners) {
        return
    }

    $owners = $listeners |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object {
            $process = Get-Process -Id $_ -ErrorAction SilentlyContinue
            if ($process) { "PID $($_) $($process.ProcessName)" } else { "PID $_" }
        }
    throw "$Name port $Port is occupied by an unmanaged process ($($owners -join ', ')). No process was stopped."
}
