# 鹅语菌 - 一键导出 MySQL 数据库
# 用途：换电脑/迁移服务器前，在本机产出可恢复的 SQL 备份。
# 用法：在仓库根目录直接运行 .\backup-db.ps1

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

# 从环境变量读取数据库连接信息
# 建议创建 .env 文件并运行前加载：Get-Content .env | ForEach-Object { $k,$v=$_.Split('=',2); [Environment]::SetEnvironmentVariable($k,$v,"Process") }
$dbName     = "earthworm"
$dbUser     = if ($env:SPRING_DATASOURCE_USERNAME) { $env:SPRING_DATASOURCE_USERNAME } else { "root" }
$dbPassword = $env:SPRING_DATASOURCE_PASSWORD
if (-not $dbPassword) {
    Write-Host "请在运行前设置环境变量 SPRING_DATASOURCE_PASSWORD，或从 .env 文件加载。" -ForegroundColor Red
    exit 1
}

if (-not (Get-Command mysqldump -ErrorAction SilentlyContinue)) {
    Write-Host "mysqldump not found. Add MySQL bin/ to PATH and try again." -ForegroundColor Red
    exit 1
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outFile   = Join-Path $root "earthworm-dump-$timestamp.sql"

Write-Host "Backing up database '$dbName' to $outFile" -ForegroundColor Cyan

$previousMysqlPwd = $env:MYSQL_PWD
try {
    # Avoid exposing the password in the mysqldump command line.
    $env:MYSQL_PWD = $dbPassword
    & mysqldump `
        --user=$dbUser `
        --single-transaction `
        --default-character-set=utf8mb4 `
        --routines --triggers `
        --add-drop-table `
        $dbName 2> "$root\backup-db.err.log" | Out-File -FilePath $outFile -Encoding UTF8
} finally {
    if ($null -eq $previousMysqlPwd) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
        $env:MYSQL_PWD = $previousMysqlPwd
    }
}

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
