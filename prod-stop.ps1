# 鹅语菌 - 生产模式停止
$ErrorActionPreference = "Continue"

function Stop-Port($port, $name) {
    $ids = Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique
    if ($ids) {
        foreach ($id in $ids) {
            Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
            Write-Host "✓ 已停掉 $name (PID $id)"
        }
    } else {
        Write-Host "  $name 端口 $port 未监听"
    }
}

Stop-Port 8080 "Backend"
Stop-Port 3000 "Frontend"

Write-Host ""
Write-Host "鹅语菌已停止" -ForegroundColor Green
