@echo off
chcp 65001 >nul
title 中大俄语 - 一键启动
cd /d D:\earthworm-main

echo ========================================
echo   中大俄语 - 一键启动
echo ========================================
echo.

:: ==========================================
:: 1️⃣ 清理端口残留进程
:: ==========================================
echo [1/5] 清理端口 8080 和 3000 残留进程...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080"') do (
    if not "%%a"=="0" taskkill /F /PID %%a >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":3000"') do (
    if not "%%a"=="0" taskkill /F /PID %%a >nul 2>&1
)
echo [OK] 端口已清理
timeout /t 2 /nobreak >nul

:: ==========================================
:: 2️⃣ 设置后端环境变量
:: ==========================================
echo [2/5] 设置环境变量...
set JWT_SECRET=YOUR_RANDOM_256_BIT_STRING
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/earthworm?useSSL=false&serverTimezone=UTC
set SPRING_DATASOURCE_USERNAME=root
set SPIRNG_DATASOURCE_PASSWORD=***REDACTED***
echo [OK] 环境变量已设置

:: ==========================================
:: 3️⃣ 生成 VBS 启动脚本（后台静默运行）
:: ==========================================
echo [3/5] 生成启动脚本...

:: 清除旧的 VBS
del /f /q "%TEMP%\start-fe.vbs" "%TEMP%\start-be.vbs" 2>nul

:: 前端启动 VBS
echo CreateObject("WScript.Shell").Run "cmd /c cd /d D:\earthworm-main\apps\client ^&^& pnpm dev --port 3000 --host 0.0.0.0 ^> D:\earthworm-main\frontend-nuxt.log 2^>^&1", 0, False > "%TEMP%\start-fe.vbs"

:: 后端启动 VBS
echo CreateObject("WScript.Shell").Run "cmd /c cd /d D:\earthworm-main ^&^& mvn -f backend\pom.xml spring-boot:run ^> D:\earthworm-main\backend-spring.log 2^>^&1", 0, False > "%TEMP%\start-be.vbs"

echo [OK] 启动脚本已生成

:: ==========================================
:: 4️⃣ 启动前后端
:: ==========================================
echo [4/5] 启动后端 Spring Boot（端口 8080）...
wscript //nologo "%TEMP%\start-be.vbs"
echo      日志: backend-spring.log

echo 启动前端 Nuxt（端口 3000）...
wscript //nologo "%TEMP%\start-fe.vbs"
echo      日志: frontend-nuxt.log

:: ==========================================
:: 5️⃣ 等待服务就绪
:: ==========================================
echo [5/5] 等待服务启动（最长 90 秒）...

set BACKEND_OK=0
for /l %%i in (1,1,30) do (
    >nul 2>&1 curl -s http://localhost:8080/actuator/health && set BACKEND_OK=1 && goto backend_ok
    >nul 2>&1 curl -s http://localhost:8080/course-pack && set BACKEND_OK=1 && goto backend_ok
    timeout /t 2 /nobreak >nul
)
:backend_ok

set FRONTEND_OK=0
for /l %%i in (1,1,30) do (
    >nul 2>&1 curl -s http://localhost:3000 && set FRONTEND_OK=1 && goto frontend_ok
    timeout /t 2 /nobreak >nul
)
:frontend_ok

:: ==========================================
:: 完成
:: ==========================================
echo.
echo ========================================
if "%BACKEND_OK%"=="1" ( echo [OK] 后端 http://localhost:8080 ) else ( echo [!!] 后端启动异常，请检查 backend-spring.log )
if "%FRONTEND_OK%"=="1" ( echo [OK] 前端 http://localhost:3000 ) else ( echo [!!] 前端启动异常，请检查 frontend-nuxt.log )
echo ========================================
if not "%BACKEND_OK%"=="1" echo 查看日志: type backend-spring.log ^| more
if not "%FRONTEND_OK%"=="1" echo 查看日志: type frontend-nuxt.log ^| more
echo.
echo 按任意键关闭本窗口（不影响服务器运行）
pause >nul
