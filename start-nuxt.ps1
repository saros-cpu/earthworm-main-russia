Start-Process -FilePath "D:\earthworm-main\apps\client\node_modules\.bin\nuxt.cmd" `
    -ArgumentList "dev --port 3000" `
    -WorkingDirectory "D:\earthworm-main\apps\client" `
    -NoNewWindow `
    -RedirectStandardOutput "D:\earthworm-main\frontend-nuxt3.log" `
    -RedirectStandardError "D:\earthworm-main\frontend-nuxt3.err.log"
Write-Host "Nuxt dev server starting on http://localhost:3000"
