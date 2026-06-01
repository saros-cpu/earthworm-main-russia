$ErrorActionPreference = "Continue"

$checks = @(
    @{ Name = "Frontend home"; Url = "http://localhost:3000"; Method = "GET" },
    @{ Name = "Frontend login page"; Url = "http://localhost:3000/login"; Method = "GET" },
    @{ Name = "Course pack page"; Url = "http://localhost:3000/course-pack"; Method = "GET" },
    @{ Name = "Media course page"; Url = "http://localhost:3000/media-course"; Method = "GET" },
    @{ Name = "Backend health"; Url = "http://localhost:8080/actuator/health"; Method = "GET" },
    @{ Name = "Backend direct course API"; Url = "http://localhost:8080/course-pack"; Method = "GET" },
    @{ Name = "Backend proxied course API"; Url = "http://localhost:3000/api/backend/course-pack"; Method = "GET" }
)

function Get-Status($check) {
    try {
        $r = Invoke-WebRequest -Uri $check.Url -Method $check.Method -TimeoutSec 10 -UseBasicParsing
        [PSCustomObject]@{ Name = $check.Name; Status = $r.StatusCode; Ok = $true; Url = $check.Url }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { "FAIL" }
        [PSCustomObject]@{ Name = $check.Name; Status = $status; Ok = $false; Url = $check.Url }
    }
}

$results = foreach ($check in $checks) { Get-Status $check }

$results | Format-Table -AutoSize

if ($results.Ok -contains $false) {
    Write-Host "Health check failed." -ForegroundColor Red
    exit 1
}

Write-Host "Health check passed." -ForegroundColor Green
