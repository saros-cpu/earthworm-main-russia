# 鹅语菌 - 环境一键安装脚本
# 使用 winget / choco 自动安装缺失的依赖

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

function Write-Step($msg) {
    Write-Host "`n==> $msg" -ForegroundColor Cyan
}

function Write-Ok($msg) {
    Write-Host "  [OK] $msg" -ForegroundColor Green
}

function Write-Skip($msg) {
    Write-Host "  [SKIP] $msg" -ForegroundColor Yellow
}

function Write-Fail($msg) {
    Write-Host "  [FAIL] $msg" -ForegroundColor Red
}

function Test-Command($name) {
    try { Get-Command $name -ErrorAction Stop | Out-Null; return $true }
    catch { return $false }
}

function Install-WithWinget($id, $name) {
    if (Test-Command "winget") {
        Write-Host "  Installing $name via winget..."
        winget install --id $id --silent --accept-package-agreements --accept-source-agreements 2>&1 | Out-Null
        return $LASTEXITCODE -eq 0
    }
    return $false
}

function Install-WithChoco($pkg, $name) {
    if (Test-Command "choco") {
        Write-Host "  Installing $name via choco..."
        choco install $pkg -y --no-progress 2>&1 | Out-Null
        return $LASTEXITCODE -eq 0
    }
    return $false
}

# ============================================================
Write-Host "╔══════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║       鹅语菌 - 环境依赖一键安装              ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Magenta

# ---- Git ----
Write-Step "Git"
if (Test-Command "git") {
    $ver = git --version
    Write-Ok "已安装: $ver"
} else {
    Write-Host "  Installing Git..."
    Install-WithWinget "Git.Git" "Git" -or (Install-WithChoco "git" "Git")
}

# ---- Java 21 ----
Write-Step "Java 21 (JDK)"
$javaOk = $false
if (Test-Command "java") {
    $ver = java -version 2>&1
    if ($ver -match '"(\d+)') {
        $jver = [int]$Matches[1]
        if ($jver -ge 21) {
            Write-Ok "已安装: Java $jver"
            $javaOk = $true
        }
    }
}
if (-not $javaOk) {
    Write-Host "  Installing Eclipse Temurin JDK 21..."
    $ok = Install-WithWinget "EclipseAdoptium.Temurin.21.JDK" "JDK 21"
    if (-not $ok) { $ok = Install-WithChoco "temurin21" "JDK 21" }
    if (-not $ok) { Write-Fail "请手动安装 JDK 21: https://adoptium.net" }
}

# ---- Maven ----
Write-Step "Maven"
if (Test-Command "mvn") {
    $ver = mvn --version 2>&1 | Select-String "Apache Maven"
    if ($ver) {
        Write-Ok "已安装: $($ver.ToString().Trim())"
    }
} else {
    Write-Host "  Installing Maven..."
    $ok = Install-WithWinget "Apache.Maven" "Maven"
    if (-not $ok) { $ok = Install-WithChoco "maven" "Maven" }
    if (-not $ok) { Write-Fail "请手动安装 Maven: https://maven.apache.org/download.cgi" }
}

# ---- Node.js ----
Write-Step "Node.js"
$nodeOk = $false
if (Test-Command "node") {
    $ver = node --version
    $verNum = $ver -replace '[v\s]', ''
    $parts = $verNum -split '\.'
    if ([int]$parts[0] -ge 20) {
        Write-Ok "已安装: Node.js $verNum"
        $nodeOk = $true
    } else {
        Write-Skip "版本过低: Node.js $verNum (需要 >= 20)"
    }
}
if (-not $nodeOk) {
    Write-Host "  Installing Node.js LTS..."
    $ok = Install-WithWinget "OpenJS.NodeJS.LTS" "Node.js LTS"
    if (-not $ok) { $ok = Install-WithChoco "nodejs-lts" "Node.js LTS" }
    if (-not $ok) { Write-Fail "请手动安装 Node.js: https://nodejs.org" }
}

# ---- pnpm ----
Write-Step "pnpm"
if (Test-Command "pnpm") {
    $ver = pnpm --version
    Write-Ok "已安装: pnpm $ver"
} else {
    Write-Host "  Installing pnpm via npm..."
    npm install -g pnpm 2>&1 | Out-Null
    if (Test-Command "pnpm") {
        $ver = pnpm --version
        Write-Ok "pnpm $ver 安装成功"
    } else {
        Write-Fail "pnpm 安装失败，请手动安装: npm install -g pnpm"
    }
}

# ---- MySQL ----
Write-Step "MySQL 8"
$mysqlOk = $false
if (Test-Command "mysql") {
    $ver = mysql --version 2>&1
    if ($ver -match 'Distrib (\d+\.\d+)') {
        Write-Ok "已安装: MySQL $($Matches[1])"
        $mysqlOk = $true
    }
}
if (-not $mysqlOk) {
    Write-Host "  Installing MySQL 8..."
    $ok = Install-WithWinget "Oracle.MySQL" "MySQL 8"
    if (-not $ok) { $ok = Install-WithChoco "mysql" "MySQL 8" }
    if (-not $ok) { Write-Fail "请手动安装 MySQL 8: https://dev.mysql.com/downloads/installer/" }
}

# ---- pnpm install ----
Write-Step "项目依赖安装"
if (Test-Command "pnpm") {
    Write-Host "  Running pnpm install..."
    Set-Location $root
    pnpm install 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Ok "pnpm install 完成"
    } else {
        Write-Fail "pnpm install 失败，请手动运行: pnpm install"
    }
}

# ============================================================
Write-Host "`n╔══════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║  环境检查完成                                ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""
Write-Host "确认 MySQL 服务已启动，然后运行以下命令启动项目：" -ForegroundColor Yellow
Write-Host "  .\run-local.ps1" -ForegroundColor White
