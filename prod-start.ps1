# 鹅语菌 - 生产模式启动（迁移到固定 IP 服务器后日常用）
# 假设：jar 已打好（mvn package），.output 已构建好（pnpm build）

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

# 让前端 Nitro 监听全部接口（外网可访问）
if (-not $env:NITRO_HOST) { $env:NITRO_HOST = "0.0.0.0" }
if (-not $env:NITRO_PORT) { $env:NITRO_PORT = "3000" }

# 把 User / Machine 范围的 OPENROUTER_* / SPRING_DATASOURCE_* 注入到当前进程
foreach ($scope in @("User", "Machine")) {
    foreach ($name in @(
        "OPENROUTER_API_KEY", "OPENROUTER_BASE_URL", "OPENROUTER_MODEL",
        "OPENROUTER_SITE_URL", "OPENROUTER_APP_NAME", "AI_PROVIDER",
        "SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD",
        "JWT_SECRET"
    )) {
        if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
            $value = [Environment]::GetEnvironmentVariable($name, $scope)
            if ($value) { [Environment]::SetEnvironmentVariable($name, $value, "Process") }
        }
    }
}

function Stop-Port($port) {
    Get-NetTCPConnection -State Listen -LocalPort $port -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique |
        ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }
}

function Wait-Http($url, $name, $timeoutSec = 60) {
    for ($i = 0; $i -lt $timeoutSec; $i++) {
        try {
            $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
            if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 500) {
                Write-Host "$name ready: $url"
                return
            }
        } catch {}
        Start-Sleep -Seconds 1
    }
    throw "$name did not become ready within $timeoutSec sec: $url"
}

# -- locate artifacts ----------------------------------------------------
$jar = Get-ChildItem -Path "$root\backend\target\*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '\.original$' } | Select-Object -First 1
if (-not $jar) {
    Write-Host "❌ 找不到 backend/target/*.jar — 先跑 mvn -f backend/pom.xml -DskipTests package" -ForegroundColor Red
    exit 1
}

$nitroEntry = "$root\apps\client\.output\server\index.mjs"
if (-not (Test-Path $nitroEntry)) {
    Write-Host "❌ 找不到 apps/client/.output/server/index.mjs — 先跑 pnpm --filter client build" -ForegroundColor Red
    exit 1
}

# -- start ---------------------------------------------------------------
Write-Host "==> 停掉 8080 / 3000 上的旧进程"
Stop-Port 8080
Stop-Port 3000
Start-Sleep -Seconds 1

Write-Host "==> 启动后端 jar => $($jar.Name)"
Start-Process `
    -FilePath "java" `
    -ArgumentList "-jar","`"$($jar.FullName)`"" `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$root\backend-prod.log" `
    -RedirectStandardError "$root\backend-prod.err.log" `
    -WindowStyle Hidden

Wait-Http "http://localhost:8080/admin/stats" "Backend"

Write-Host "==> 启动前端 Nitro (host=$env:NITRO_HOST port=$env:NITRO_PORT)"
Start-Process `
    -FilePath "node" `
    -ArgumentList "`"$nitroEntry`"" `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$root\frontend-prod.log" `
    -RedirectStandardError "$root\frontend-prod.err.log" `
    -WindowStyle Hidden

Wait-Http "http://localhost:3000" "Frontend"

# -- summary -------------------------------------------------------------
$ip = (Get-NetIPAddress -AddressFamily IPv4 -PrefixOrigin Dhcp,Manual -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notlike "169.*" -and $_.IPAddress -ne "127.0.0.1" } |
    Select-Object -First 1).IPAddress

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host " 鹅语菌生产模式已启动" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "本机访问 : http://localhost:3000"
if ($ip) { Write-Host "局域网/公网: http://$ip`:3000  ← 把这个 URL 发给别人" -ForegroundColor Yellow }
Write-Host ""
Write-Host "日志:"
Write-Host "  backend-prod.log / backend-prod.err.log"
Write-Host "  frontend-prod.log / frontend-prod.err.log"
Write-Host ""
Write-Host "停止: .\prod-stop.ps1"
