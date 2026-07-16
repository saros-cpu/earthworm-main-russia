$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
$runtimeDir = Join-Path $root "runtime"
if (-not (Test-Path $runtimeDir)) { New-Item -ItemType Directory -Path $runtimeDir | Out-Null }

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$versionFile = Join-Path $runtimeDir "build-version.txt"

# ─── 后端构建 ───
Write-Host "=== Building backend jar ===" -ForegroundColor Cyan
$beOk = $true
try {
    mvn -f backend\pom.xml -DskipTests package *>&1 | Tee-Object -FilePath "$runtimeDir\backend-build-$timestamp.log"
    $jar = Get-ChildItem -Path "$root\backend\target\*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch '\.original$' } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($jar) {
        Write-Host "Backend jar: $($jar.Name) ($('{0:N0}' -f ($jar.Length/1KB)) KB)" -ForegroundColor Green
    } else {
        Write-Host "No backend jar found!" -ForegroundColor Red
        $beOk = $false
    }
} catch {
    Write-Host "Backend build failed: $_" -ForegroundColor Red
    $beOk = $false
}

# ─── 前端构建（SSR 模式） ───
Write-Host "=== Building frontend (SSR) ===" -ForegroundColor Cyan
$feOk = $true
try {
    pnpm --filter client build 2>&1 | Tee-Object -FilePath "$runtimeDir\frontend-build-$timestamp.log"
    $serverOk = Test-Path "$root\apps\client\.output\server\index.mjs"
    $nuxtDir = "$root\apps\client\.output\public\_nuxt"
    $nuxtOk = Test-Path $nuxtDir -and (Get-ChildItem "$nuxtDir\*.js" | Select-Object -First 1)
    if ($serverOk -and $nuxtOk) {
        Write-Host "Frontend SSR output ready ($( (Get-ChildItem "$nuxtDir\*.js").Count ) JS chunks)" -ForegroundColor Green
    } else {
        if (-not $serverOk) { Write-Host "Frontend SSR server entry not found!" -ForegroundColor Red }
        if (-not $nuxtOk) { Write-Host "Frontend _nuxt/ directory missing or empty!" -ForegroundColor Red }
        $feOk = $false
    }
} catch {
    Write-Host "Frontend build failed: $_" -ForegroundColor Red
    $feOk = $false
}

# ─── 输出构建版本信息 ───
$buildInfo = @"
Build: $timestamp
Backend: $(if ($beOk) { "OK" } else { "FAILED" })
Frontend: $(if ($feOk) { "OK" } else { "FAILED" })
"@
Set-Content -Path $versionFile -Value $buildInfo

Write-Host ""
Write-Host "=== Build Summary ===" -ForegroundColor Cyan
Write-Host $buildInfo
if ($beOk -and $feOk) {
    Write-Host "Build completed successfully." -ForegroundColor Green
    Write-Host "Start: .\prod-start.ps1"
} else {
    Write-Host "Build completed with errors." -ForegroundColor Red
    exit 1
}
