@echo off
REM Deprecated. Use .\prod-start.ps1.
cd /d D:\earthworm-main
if "%JWT_SECRET%"=="" (
    echo [ERROR] JWT_SECRET must be set before startup. Use .\prod-start.ps1.
    exit /b 1
)
if "%SPRING_DATASOURCE_PASSWORD%"=="" (
    echo [ERROR] SPRING_DATASOURCE_PASSWORD must be set before startup. Use .\prod-start.ps1.
    exit /b 1
)
start "BE" cmd /c "java -jar backend\target\backend-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0 > backend-prod.log 2>&1"
