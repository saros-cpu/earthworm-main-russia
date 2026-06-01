$ErrorActionPreference = "Continue"

function Show-Port($port, $name) {
    $items = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue
    if (-not $items) {
        Write-Host "$name port $port is not listening" -ForegroundColor Yellow
        return
    }
    foreach ($item in $items) {
        $p = Get-Process -Id $item.OwningProcess -ErrorAction SilentlyContinue
        Write-Host "$name port $port listening: PID $($item.OwningProcess) $($p.ProcessName)" -ForegroundColor Green
    }
}

function Test-Url($url, $name) {
    try {
        $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        Write-Host "$name HTTP $($r.StatusCode): $url" -ForegroundColor Green
    } catch {
        Write-Host "$name failed: $url" -ForegroundColor Yellow
    }
}

Show-Port 8080 "Backend"
Show-Port 3000 "Frontend"
Test-Url "http://localhost:8080/course-pack" "Backend"
Test-Url "http://localhost:3000" "Frontend"
