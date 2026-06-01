# Local launcher for the no-Docker Spring Boot + Nuxt setup.
$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
. (Join-Path $root "scripts\prod-process-control.ps1")

foreach ($name in @("AI_PROVIDER", "OPENROUTER_API_KEY", "OPENROUTER_BASE_URL", "OPENROUTER_MODEL", "OPENROUTER_SITE_URL", "OPENROUTER_APP_NAME")) {
    if (-not [Environment]::GetEnvironmentVariable($name, "Process")) {
        $value = [Environment]::GetEnvironmentVariable($name, "User")
        if ($value) {
            [Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

function Wait-Http($url, $name) {
    for ($i = 0; $i -lt 45; $i++) {
        try {
            $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                Write-Host "$name is ready: $url"
                return
            }
        } catch {}
        Start-Sleep -Seconds 1
    }

    throw "$name did not become ready: $url"
}

Write-Host "Checking that local ports 8080 and 3000 are available..."
Assert-PortAvailable 8080 "Backend"
Assert-PortAvailable 3000 "Frontend"

Write-Host "Starting Spring Boot backend on http://localhost:8080 ..."
Start-Process `
    -FilePath "mvn" `
    -ArgumentList "-f backend/pom.xml spring-boot:run" `
    -WorkingDirectory $root `
    -RedirectStandardOutput "$root\backend-spring.log" `
    -RedirectStandardError "$root\backend-spring.err.log" `
    -WindowStyle Hidden

Wait-Http "http://localhost:8080/course-pack" "Backend"

$pnpm = (Get-Command pnpm.cmd -ErrorAction Stop).Source

Write-Host "Starting Nuxt frontend on http://localhost:3000 ..."
Start-Process `
    -FilePath $pnpm `
    -ArgumentList "dev --port 3000 --host 0.0.0.0" `
    -WorkingDirectory "$root\apps\client" `
    -RedirectStandardOutput "$root\frontend-nuxt.log" `
    -RedirectStandardError "$root\frontend-nuxt.err.log" `
    -WindowStyle Hidden

Wait-Http "http://localhost:3000" "Frontend"

Write-Host ""
Write-Host "Local app is running."
Write-Host "Frontend: http://localhost:3000"
Write-Host "Backend:  http://localhost:8080"
Write-Host "Backend log:  backend-spring.log"
Write-Host "Frontend log: frontend-nuxt.log"
