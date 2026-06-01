@echo off
cd /d D:\earthworm-main\apps\client
pnpm dev --port 3000 --host 0.0.0.0 > D:\earthworm-main\frontend-nuxt.log 2>&1
