@echo off
cd /d D:\earthworm-main
:: Use the default secret from application.yml (34 chars = 272 bits, enough for HS256)
start "BE" cmd /c "java -jar backend\target\backend-0.0.1-SNAPSHOT.jar --server.address=0.0.0.0 --jwt.secret=CHANGE_ME_TO_RANDOM_256_BIT_STRING_12345 > backend-prod.log 2>&1"
