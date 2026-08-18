$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$runtimeDir = if ($env:EARTHWORM_RUNTIME_DIR) { $env:EARTHWORM_RUNTIME_DIR } else { Join-Path $root "runtime" }
Set-Location $root

# ─── 加载 .env ───
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
}

# ─── 从系统环境变量补充缺失值 ───
foreach ($scope in @("User", "Machine")) {
    foreach ($name in @("JWT_SECRET","SPRING_DATASOURCE_URL","SPRING_DATASOURCE_USERNAME","SPRING_DATASOURCE_PASSWORD","OPENROUTER_API_KEY","MEDIA_ROOT_PATH","FFMPEG_PATH","CORS_ALLOWED_ORIGINS")) {
        if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
            $value = [Environment]::GetEnvironmentVariable($name, $scope)
            if ($value) { [Environment]::SetEnvironmentVariable($name, $value, "Process") }
        }
    }
}

# ─── 验证必填变量 ───
$required = @("SPRING_DATASOURCE_PASSWORD", "JWT_SECRET")
$missing = $required | Where-Object { -not [Environment]::GetEnvironmentVariable($_, "Process") }
if ($missing) {
    Write-Host "Missing required env vars: $($missing -join ', ')" -ForegroundColor Red
    Write-Host "Fill them in .env or set as system environment variables." -ForegroundColor Yellow
    exit 1
}

. (Join-Path $root "scripts\prod-process-control.ps1")

# ─── 端口检查 ───
Assert-PortAvailable 8000 "Backend"
Assert-PortAvailable 3000 "Frontend"

# ─── 找后端 jar ───
$jar = Get-ChildItem "$root\backend\target\*.jar" |
    Where-Object { $_.Name -notmatch '\.original$' } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1
if (-not $jar) {
    Write-Host "Missing backend jar. Run: .\prod-build.ps1" -ForegroundColor Red
    exit 1
}

# ─── 启动后端 ───
Write-Host "Starting backend: $($jar.Name)"
$jvmArgs = @(
    "-Dspring.profiles.active=prod"
    "-Dserver.servlet.context-path=/api/v1"
    "-Dcors.allowedOrigins=$env:CORS_ALLOWED_ORIGINS"
    "-jar", "`"$($jar.FullName)`""
)
$beProc = Start-Process `
    -FilePath "java" `
    -ArgumentList $jvmArgs `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$runtimeDir\backend-prod.log" `
    -RedirectStandardError "$runtimeDir\backend-prod.err.log" `
    -WindowStyle Hidden `
    -PassThru
Save-ManagedProcessRecord $runtimeDir "backend" $beProc

# ─── 等待后端就绪（最多 90 秒） ───
Write-Host "Waiting for backend ..."
$backendReady = $false
for ($i = 0; $i -lt 90; $i++) {
    try {
        $null = Invoke-WebRequest -Uri "http://127.0.0.1:8000/api/v1/course-pack" -UseBasicParsing -TimeoutSec 3
        $backendReady = $true; break
    } catch [System.Net.WebException] { $backendReady = $true; break }
    catch {}
    Start-Sleep -Seconds 1
}
if (-not $backendReady) {
    Stop-ManagedProcess $runtimeDir "backend" | Out-Null
    Write-Host "Backend did not start within 90 seconds." -ForegroundColor Red
    exit 1
}
Write-Host "Backend ready."

# ─── 启动前端 SSR ───
$nitro = "$root\apps\client\.output\server\index.mjs"
if (-not (Test-Path $nitro)) {
    Stop-ManagedProcess $runtimeDir "backend" | Out-Null
    Write-Host "Missing Nuxt SSR output. Run: .\prod-build.ps1" -ForegroundColor Red
    exit 1
}

Write-Host "Starting frontend SSR ..."
$feProc = Start-Process `
    -FilePath "node" `
    -ArgumentList "`"$nitro`"" `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$runtimeDir\frontend-prod.log" `
    -RedirectStandardError "$runtimeDir\frontend-prod.err.log" `
    -WindowStyle Hidden `
    -PassThru
Save-ManagedProcessRecord $runtimeDir "frontend" $feProc

# ─── 等待前端就绪（最多 60 秒） ───
Write-Host "Waiting for frontend ..."
$frontendReady = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:3000" -UseBasicParsing -TimeoutSec 3
        if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) { $frontendReady = $true; break }
    } catch {}
    Start-Sleep -Seconds 1
}
if (-not $frontendReady) {
    Stop-ManagedProcess $runtimeDir "backend" | Out-Null
    Stop-ManagedProcess $runtimeDir "frontend" | Out-Null
    Write-Host "Frontend did not start within 60 seconds." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Earthworm platform started." -ForegroundColor Green
Write-Host "Frontend: http://localhost:3000"
Write-Host "Backend:  http://localhost:8000"
Write-Host "Stop: .\prod-stop.ps1"
