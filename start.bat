@echo off
chcp 65001 >nul
title 俄语学习平台 - 一键启动
cd /d D:\earthworm-main

REM ⚠ 此脚本仅用于开发模式（pnpm dev），生产环境请使用 .\prod-start.ps1

echo ========================================
echo   俄语学习平台 - 一键启动
echo ========================================
echo.

:: ==========================================
:: 1️⃣ 确认服务端口可用
:: ==========================================
echo [1/5] 检查端口 8080 和 3000...
powershell -NoProfile -ExecutionPolicy Bypass -Command ". '.\scripts\prod-process-control.ps1'; Assert-PortAvailable 8080 'Backend'; Assert-PortAvailable 3000 'Frontend'"
if errorlevel 1 (
    echo [ERROR] 端口已被其他程序占用，未结束任何进程。
    exit /b 1
)
echo [OK] 端口可用

:: ==========================================
:: 2️⃣ 设置后端环境变量
:: ==========================================
echo [2/5] 设置环境变量...
if "%JWT_SECRET%"=="" (
    echo [ERROR] JWT_SECRET must be set before startup.
    exit /b 1
)
if "%SPRING_DATASOURCE_URL%"=="" set SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/earthworm?useSSL=false^&serverTimezone=UTC
if "%SPRING_DATASOURCE_USERNAME%"=="" set SPRING_DATASOURCE_USERNAME=reader
if "%SPRING_DATASOURCE_PASSWORD%"=="" (
    echo [ERROR] SPRING_DATASOURCE_PASSWORD must be set before startup.
    exit /b 1
)
REM ⚠ 密码已从代码中移除。请在系统环境变量中设置 SPRING_DATASOURCE_PASSWORD，或在运行前 set 该变量。
REM set SPRING_DATASOURCE_PASSWORD=你的密码
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
