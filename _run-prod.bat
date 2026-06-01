@echo off
REM Deprecated. Use .\prod-start.ps1 for validated environment startup.
cd /d D:\earthworm-main

:: Set env vars
set NITRO_HOST=0.0.0.0
set NITRO_PORT=3000
if "%JWT_SECRET%"=="" (
    echo [ERROR] JWT_SECRET must be set before startup. Use .\prod-start.ps1.
    exit /b 1
)
if "%SPRING_DATASOURCE_PASSWORD%"=="" (
    echo [ERROR] SPRING_DATASOURCE_PASSWORD must be set before startup. Use .\prod-start.ps1.
    exit /b 1
)

:: Start backend
start "BE" cmd /c "java -jar backend\target\backend-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0 > backend-prod.log 2>&1"

:: Wait for backend
echo Waiting for backend...
:waitbe
timeout /t 2 /nobreak >nul
powershell -Command "try{$r=Invoke-WebRequest -Uri http://localhost:8080/course-pack -UseBasicParsing -TimeoutSec 2; if($r.StatusCode -ge 200){exit 0}}catch{}" 2>nul
if errorlevel 1 goto waitbe

:: Start frontend (using static SPA server)
start "FE" cmd /c "node scripts\prod-frontend-server.mjs > frontend-prod.log 2>&1"

echo Backend and Frontend started
echo Frontend: http://109.71.228.50:3000
