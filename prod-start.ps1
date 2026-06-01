$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
$runtimeDir = if ($env:EARTHWORM_RUNTIME_DIR) { $env:EARTHWORM_RUNTIME_DIR } else { Join-Path $root "runtime" }
if (-not (Test-Path $runtimeDir)) { New-Item -ItemType Directory -Path $runtimeDir | Out-Null }
. (Join-Path $root "scripts\prod-process-control.ps1")

# ─── 加载 .env 文件（如有） ───
$envFile = Join-Path $root ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#=]+)=(.*)\s*$') {
            $k = $matches[1].Trim()
            $v = $matches[2].Trim()
            if (-not [Environment]::GetEnvironmentVariable($k, "Process")) {
                [Environment]::SetEnvironmentVariable($k, $v, "Process")
            }
        }
    }
    Write-Host "Loaded .env file"
}

# ─── 从 User/Machine 范围补充缺失变量 ───
foreach ($scope in @("User", "Machine")) {
    foreach ($name in @(
        "OPENROUTER_API_KEY", "OPENROUTER_BASE_URL", "OPENROUTER_MODEL",
        "OPENROUTER_SITE_URL", "OPENROUTER_APP_NAME", "AI_PROVIDER",
        "SPRING_DATASOURCE_URL", "SPRING_DATASOURCE_USERNAME", "SPRING_DATASOURCE_PASSWORD",
        "JWT_SECRET", "CORS_ALLOWED_ORIGINS", "MEDIA_ROOT_PATH", "FFMPEG_PATH",
        "MEDIA_CACHE_MAX_BYTES", "MEDIA_TRANSCODE_WORKERS", "MEDIA_TRANSCODE_QUEUE_CAPACITY",
        "TTS_MAX_TEXT_LENGTH", "TTS_REQUESTS_PER_MINUTE", "TTS_CACHE_MAX_BYTES"
    )) {
        if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
            $value = [Environment]::GetEnvironmentVariable($name, $scope)
            if ($value) { [Environment]::SetEnvironmentVariable($name, $value, "Process") }
        }
    }
}

# ─── 设置前端服务默认值（非敏感，允许 fallback） ───
if (-not $env:NITRO_HOST) { $env:NITRO_HOST = "0.0.0.0" }
if (-not $env:NITRO_PORT) { $env:NITRO_PORT = "3000" }
if (-not $env:HOST) { $env:HOST = $env:NITRO_HOST }
if (-not $env:PORT) { $env:PORT = $env:NITRO_PORT }
if (-not $env:API_BASE) { $env:API_BASE = "/api/backend" }
if (-not $env:BACKEND_ENDPOINT) { $env:BACKEND_ENDPOINT = "/api/backend/" }

# ─── 验证必填敏感变量 ───
$required = @("SPRING_DATASOURCE_PASSWORD", "JWT_SECRET")
$missing = $required | Where-Object { -not [Environment]::GetEnvironmentVariable($_, "Process") }
if ($missing) {
    Write-Host "缺失敏感环境变量，无法启动:" -ForegroundColor Red
    $missing | ForEach-Object { Write-Host "  - $_" }
    Write-Host "请创建 .env 文件（参考 .env 模板）或在系统环境变量中设置。" -ForegroundColor Yellow
    exit 1
}

function Wait-Http($url, $name, $timeoutSec = 60) {
    for ($i = 0; $i -lt $timeoutSec; $i++) {
        try {
            $code = & curl.exe -s -o NUL -w "%{http_code}" --max-time 3 $url
            if ([int]$code -ge 200 -and [int]$code -lt 500) {
                Write-Host "$name ready: $url"
                return
            }
        } catch {}
        Start-Sleep -Seconds 1
    }
    throw "$name did not become ready within $timeoutSec sec: $url"
}

$jar = Get-ChildItem -Path "$root\backend\target\*.jar" -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notmatch '\.original$' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $jar) {
    Write-Host "Missing backend jar. Run: .\prod-build.ps1" -ForegroundColor Red
    exit 1
}

# SSR 模式下使用 Nitro 服务端（.output/server/index.mjs），替代 prod-frontend-server.mjs 纯静态服务器
$nitroServer = "$root\apps\client\.output\server\index.mjs"
if (-not (Test-Path $nitroServer)) {
    Write-Host "Missing Nuxt SSR output. Run: .\prod-build.ps1" -ForegroundColor Red
    exit 1
}

Write-Host "Stopping processes previously started by this launcher"
Stop-ManagedProcess $runtimeDir "backend" | Out-Null
Stop-ManagedProcess $runtimeDir "frontend" | Out-Null
Assert-PortAvailable 8080 "Backend"
Assert-PortAvailable 3000 "Frontend"
Start-Sleep -Seconds 1

Write-Host "Starting backend jar: $($jar.Name)"
$jvmArgs = @(
    "-Dspring.profiles.active=prod"
    "-Dcors.allowedOrigins=$env:CORS_ALLOWED_ORIGINS"
    "-jar", "`"$($jar.FullName)`""
)
$backendProcess = Start-Process `
    -FilePath "java" `
    -ArgumentList $jvmArgs `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$runtimeDir\backend-prod.log" `
    -RedirectStandardError "$runtimeDir\backend-prod.err.log" `
    -WindowStyle Hidden `
    -PassThru
Save-ManagedProcessRecord $runtimeDir "backend" $backendProcess

try {
    Wait-Http "http://localhost:8080/course-pack" "Backend" 90
} catch {
    Stop-ManagedProcess $runtimeDir "backend" | Out-Null
    throw
}

Write-Host "Starting Nuxt SSR server on $env:NITRO_HOST`:$env:NITRO_PORT"
$frontendProcess = Start-Process `
    -FilePath "node" `
    -ArgumentList "`"$nitroServer`"" `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$runtimeDir\frontend-prod.log" `
    -RedirectStandardError "$runtimeDir\frontend-prod.err.log" `
    -WindowStyle Hidden `
    -PassThru
Save-ManagedProcessRecord $runtimeDir "frontend" $frontendProcess

try {
    Wait-Http "http://localhost:3000" "Frontend" 60
} catch {
    Stop-ManagedProcess $runtimeDir "frontend" | Out-Null
    Stop-ManagedProcess $runtimeDir "backend" | Out-Null
    throw
}

$ip = (Get-NetIPAddress -AddressFamily IPv4 -PrefixOrigin Dhcp,Manual -ErrorAction SilentlyContinue |
    Where-Object { $_.IPAddress -notlike "169.*" -and $_.IPAddress -ne "127.0.0.1" } |
    Select-Object -First 1).IPAddress

Write-Host ""
Write-Host "Production mode started." -ForegroundColor Green
Write-Host "Local: http://localhost:3000"
if ($ip) { Write-Host "Network: http://$ip`:3000" -ForegroundColor Yellow }
Write-Host "Logs:"
Write-Host "  runtime\backend-prod.log / runtime\backend-prod.err.log"
Write-Host "  runtime\frontend-prod.log / runtime\frontend-prod.err.log"
Write-Host "Stop: .\prod-stop.ps1"
