# ⚠ 已废弃，请使用 .\prod-start.ps1（生产环境）或 .\start.bat（开发模式）
# run-all.ps1 - 一键启动后端和前端，并检测健康状态
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $root "scripts\prod-process-control.ps1")

# -------------------------------------------------
# 1️⃣ 环境变量（请自行替换 JWT secret 为随机 256-bit 字符串）
# -------------------------------------------------
if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    throw "JWT_SECRET must be set before startup."
}
if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_URL)) {
    $env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3306/earthworm?useSSL=false&serverTimezone=UTC"
}
if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_USERNAME)) {
    $env:SPRING_DATASOURCE_USERNAME = "reader"
}
if ([string]::IsNullOrWhiteSpace($env:SPRING_DATASOURCE_PASSWORD)) {
    throw "SPRING_DATASOURCE_PASSWORD must be set before startup."
}
# ⚠ 密码已从代码中移除。请在运行前设置环境变量 $env:SPRING_DATASOURCE_PASSWORD，或从 .env 文件加载。
# $env:SPRING_DATASOURCE_PASSWORD = "你的密码"

# -------------------------------------------------
# 2️⃣ 确认服务端口未被其他程序占用
# -------------------------------------------------
Assert-PortAvailable 8080 "Backend"
Assert-PortAvailable 3001 "Frontend"

# -------------------------------------------------
# 3️⃣ 启动后端（Spring Boot） → backend.log
# -------------------------------------------------
Start-Process -FilePath "cmd.exe" -ArgumentList "/c mvn -f backend/pom.xml spring-boot:run > backend.log 2>&1" -WorkingDirectory "$PWD" -NoNewWindow

# -------------------------------------------------
# 4️⃣ 启动前端（Nuxt） → frontend.log
# -------------------------------------------------
Set-Location "apps/client"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c pnpm dev > ../../frontend.log 2>&1" -WorkingDirectory "$PWD" -NoNewWindow
Set-Location "$PWD\..\.."

# -------------------------------------------------
# 5️⃣ 检查后端是否成功启动（最多等待 30 秒）
# -------------------------------------------------
$backendReady = $false
for ($i=0; $i -lt 30; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri http://localhost:8080/actuator/health -UseBasicParsing -TimeoutSec 2
        if ($resp.StatusCode -eq 200) { $backendReady = $true; break }
    } catch {}
    Start-Sleep -Seconds 1
}
if (-not $backendReady) {
    Write-Host "\n❌ 后端未在 30 秒内启动成功！查看日志：backend.log\n"
    Get-Content backend.log -Tail 20 | Write-Host
    exit 1
} else {
    Write-Host "\n✅ 后端已启动并返回健康检查。"
}

# -------------------------------------------------
# 6️⃣ 检查前端是否成功启动（最多等待 30 秒）
# -------------------------------------------------
$frontendReady = $false
for ($i=0; $i -lt 30; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri http://localhost:3001 -UseBasicParsing -TimeoutSec 2
        if ($resp.StatusCode -eq 200) { $frontendReady = $true; break }
    } catch {}
    Start-Sleep -Seconds 1
}
if (-not $frontendReady) {
    Write-Host "\n❌ 前端未在 30 秒内启动成功！查看日志：frontend.log\n"
    Get-Content frontend.log -Tail 20 | Write-Host
    exit 1
} else {
    Write-Host "\n✅ 前端已启动并返回 200 OK。"
}

# -------------------------------------------------
# 7️⃣ 成功提示
# -------------------------------------------------
Write-Host "\n🚀 项目已完整启动！"
Write-Host "后端 API： http://localhost:8080/api"
Write-Host "前端 UI： http://localhost:3001"
