@echo off
cd /d D:\earthworm-main\apps\client\.output\server
set NITRO_HOST=0.0.0.0
set NITRO_PORT=3000
start "FE" cmd /c "node index.mjs > D:\earthworm-main\frontend-prod.log 2>&1"
