@echo off
REM ⚠ 已废弃，请使用 .\prod-start.ps1
REM 此文件曾含有硬编码 JWT secret，已从版本控制中移除。如需使用请自行设置 JWT_SECRET 环境变量。
REM set JWT_SECRET=你的密钥
set OPENAI_ENABLED=false
cd /d D:\earthworm-main\backend
start /B C:\Users\User\apps\apache-maven-3.9.15\bin\mvn.cmd spring-boot:run -DskipTests > D:\earthworm-main\backend\backend.log 2>&1
