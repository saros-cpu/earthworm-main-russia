$log = "D:\earthworm-main\backend-spring.log"
$err = "D:\earthworm-main\backend-spring.err.log"
$proc = Start-Process -FilePath "mvn" -ArgumentList "-f D:\earthworm-main\backend\pom.xml spring-boot:run" -WorkingDirectory "D:\earthworm-main" -RedirectStandardOutput $log -RedirectStandardError $err -WindowStyle Hidden -PassThru
$proc.Id | Out-File "D:\earthworm-main\.be.pid"
