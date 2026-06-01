@echo off
cd /d "%~dp0apps\client"
if not defined NUXT_PUBLIC_API_BASE set "NUXT_PUBLIC_API_BASE=http://localhost:8080"
if not defined DEV_ORIGIN set "DEV_ORIGIN=http://localhost:3000"
if not defined DEV_HOST set "DEV_HOST=0.0.0.0"
npx nuxt dev --port 3000 --host %DEV_HOST%
