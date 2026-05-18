@echo off
cd /d D:\earthworm-main

:: Set env vars
set NITRO_HOST=0.0.0.0
set NITRO_PORT=3000
set JWT_SECRET=dev-temp-jwt-secret-2026

:: Start backend
start "BE" cmd /c "java -jar backend\target\backend-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0 > backend-prod.log 2>&1"

:: Wait for backend
echo Waiting for backend...
:waitbe
timeout /t 2 /nobreak >nul
powershell -Command "try{$r=Invoke-WebRequest -Uri http://localhost:8080/admin/stats -UseBasicParsing -TimeoutSec 2; if($r.StatusCode -ge 200){exit 0}}catch{}" 2>nul
if errorlevel 1 goto waitbe

:: Start frontend
start "FE" cmd /c "node apps\client\.output\server\index.mjs > frontend-prod.log 2>&1"

echo Backend and Frontend started
echo Frontend: http://109.71.228.50:3000
