@echo off
chcp 65001 >nul

:: Use Node.js 20
set PATH=C:\Users\User\AppData\Roaming\nvm\v20.12.2;%PATH%

:: Use pnpm from the project
cd /d D:\earthworm-main\apps\client

:: Install with compatible pnpm via corepack
C:\Users\User\AppData\Roaming\nvm\v20.12.2\node.exe -e "require('fs').writeFileSync('C:\\Users\\User\\AppData\\Roaming\\nvm\\v20.12.2\\pnpm_test.txt', process.version + '\n' + process.execPath + '\n')"

echo Node version:
node --version
echo PATH:
echo %PATH%
