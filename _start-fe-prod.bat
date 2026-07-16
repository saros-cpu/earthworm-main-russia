@echo off
REM ⚠ 已废弃，请使用 .\prod-start.ps1
cd /d D:\earthworm-main
set NITRO_HOST=0.0.0.0
set NITRO_PORT=3000
start "FE" cmd /c "node scripts\prod-frontend-server.mjs > frontend-prod.log 2>&1"
