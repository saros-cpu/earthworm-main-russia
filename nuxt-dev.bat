@echo off
chcp 65001 >nul
cd /d D:\earthworm-main\apps\client
echo Starting Nuxt dev server...
D:\earthworm-main\apps\client\node_modules\.bin\nuxt.cmd dev --port 3000
