# 鹅语菌 - 一键导出 MySQL 数据库
# 用途：换电脑/迁移服务器前，在本机产出可恢复的 SQL 备份。
# 用法：在仓库根目录直接运行 .\backup-db.ps1

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

# 从 application.yml 读默认连接信息（如果你改过这里就 sync 一下）
$dbName     = "earthworm"
$dbUser     = "root"
$dbPassword = "***REDACTED***"

# 允许通过环境变量覆盖（推荐，避免把密码进 git）
if ($env:SPRING_DATASOURCE_USERNAME) { $dbUser     = $env:SPRING_DATASOURCE_USERNAME }
if ($env:SPRING_DATASOURCE_PASSWORD) { $dbPassword = $env:SPRING_DATASOURCE_PASSWORD }

if (-not (Get-Command mysqldump -ErrorAction SilentlyContinue)) {
    Write-Host "mysqldump not found. Add MySQL bin/ to PATH and try again." -ForegroundColor Red
    exit 1
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outFile   = Join-Path $root "earthworm-dump-$timestamp.sql"

Write-Host "Backing up database '$dbName' to $outFile" -ForegroundColor Cyan

# Use --password=xxx to avoid interactive password prompt
& mysqldump `
    --user=$dbUser `
    --password=$dbPassword `
    --single-transaction `
    --default-character-set=utf8mb4 `
    --routines --triggers `
    --add-drop-table `
    $dbName 2> "$root\backup-db.err.log" | Out-File -FilePath $outFile -Encoding UTF8

if ($LASTEXITCODE -ne 0) {
    Write-Host "mysqldump failed, check backup-db.err.log" -ForegroundColor Red
    Get-Content "$root\backup-db.err.log" -Tail 10
    Remove-Item -Force $outFile -ErrorAction SilentlyContinue
    exit 1
}

$size = (Get-Item $outFile).Length / 1MB
Write-Host "Backup complete:" -ForegroundColor Green
Write-Host "    File: $outFile"
Write-Host ("    Size: {0:N1} MB" -f $size)
Write-Host ""
Write-Host "Next: copy this file to the new machine and restore per DEPLOYMENT.md" -ForegroundColor Yellow
Write-Host "  mysql -u root -p $dbName < $($outFile | Split-Path -Leaf)" -ForegroundColor White
