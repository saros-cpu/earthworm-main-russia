# 生成随机 256-bit JWT 密钥
$bytes = [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
$secret = [System.Convert]::ToBase64String($bytes)
Write-Host "JWT_SECRET=$secret"
