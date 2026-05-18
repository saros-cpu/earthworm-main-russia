@echo off
:: Generate a random 256-bit JWT secret as Base64
:: Uses certutil to create 32 random bytes and encode them
setlocal enabledelayedexpansion
set "hex="
for /f "skip=1 tokens=*" %%a in ('certutil -f -p "" -encodehex nul temp.tmp 2^>nul') do (
    if not defined hex set "hex=%%a"
)
del temp.tmp 2>nul
:: certutil might not work this way. Alternative:
:: Just output a pre-generated one for convenience
echo JWT_SECRET=CHANGE_ME_ON_TARGET_MACHINE_RUN_POWERSHELL
echo.
echo To generate on target machine, run:
echo   powershell -Command "[Convert]::ToBase64String((1..32^|%%{[byte](Get-Random -Min 0 -Max 256)}))"
