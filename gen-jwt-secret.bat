@echo off
REM Generate a cryptographically random 256-bit JWT secret as Base64.
powershell -NoProfile -Command "$bytes = New-Object byte[] 32; $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create(); try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }; Write-Output ('JWT_SECRET=' + [System.Convert]::ToBase64String($bytes))"
if errorlevel 1 (
    echo [ERROR] Unable to generate JWT_SECRET.
    exit /b 1
)
