@echo off
cd /d D:\earthworm-main\apps\client
set NUXT_PUBLIC_API_BASE=http://109.71.228.50:8080
set DEV_ORIGIN=http://109.71.228.50:3000
set DEV_HOST=109.71.228.50
npx nuxt dev --port 3000
