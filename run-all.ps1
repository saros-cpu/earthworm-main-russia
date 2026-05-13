# run-all.ps1 - 一键启动后端和前端，并检测健康状态

# -------------------------------------------------
# 1️⃣ 环境变量（请自行替换 JWT secret 为随机 256-bit 字符串）
# -------------------------------------------------
$env:JWT_SECRET = "YOUR_RANDOM_256_BIT_STRING"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/earthworm?useSSL=false&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = "***REDACTED***"

# -------------------------------------------------
# 2️⃣ 结束占用 8080 的进程（如果有）
# -------------------------------------------------
$pids = (netstat -ano | Select-String ':8080' | ForEach-Object { ($_ -split '\s+')[4] }) | Where-Object { $_ -match '^\d+$' } | Sort-Object -Unique
foreach ($pid in $pids) {
    try {
        Write-Host "Ending process PID $pid that holds port 8080..."
        taskkill /PID $pid /F > $null 2>&1
    } catch {}
}

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
