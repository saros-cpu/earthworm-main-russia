@echo off
chcp 65001 >nul
cd /d D:\earthworm-main\apps\client
D:\earthworm-main\apps\client\node_modules\.bin\nuxt.cmd dev --port 3000 > D:\earthworm-main\frontend-nuxt2.log 2>&1
