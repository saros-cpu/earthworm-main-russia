@echo off
chcp 65001 >nul
echo ========================================
echo   Register Earthworm Auto-Start Task
echo ========================================
echo.

schtasks /Create /SC ONSTART /TN Earthworm-Platform /TR "powershell -NoProfile -ExecutionPolicy Bypass -File D:\earthworm-main\start-earthworm.ps1" /RU SYSTEM /RL HIGHEST /F

if errorlevel 1 (
    echo.
    echo [FAILED] Task registration failed.
    echo Try running as Administrator.
) else (
    echo.
    echo [SUCCESS] Task 'Earthworm-Platform' registered.
    echo Earthworm will auto-start on next boot.
    echo.
    echo Manual start:  schtasks /Run /TN Earthworm-Platform
    echo Manual stop:   powershell -File D:\earthworm-main\prod-stop.ps1
    echo Unregister:    schtasks /Delete /TN Earthworm-Platform /F
)

echo.
pause
