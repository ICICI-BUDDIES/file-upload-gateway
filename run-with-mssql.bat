@echo off
echo Starting services with local MSSQL Server...

REM Set environment variables for MSSQL
set SPRING_PROFILES_ACTIVE=prod
set DB_HOST=localhost
set DB_PORT=1433
set DB_NAME=templatedb
set DB_USERNAME=template_user
set DB_PASSWORD=LocalPassword123!

REM Create storage directory
if not exist "storage" mkdir storage

echo Starting Template Service on port 8081...
start "Template Service" java -jar backend\template-service\target\template-service-1.0.0.jar

REM Wait for Template Service to start
timeout /t 30 /nobreak

echo Starting Gateway Service on port 8082...
start "Gateway Service" java -jar backend\gateway-service\target\gateway-service-0.0.1-SNAPSHOT.jar

echo Both services started with MSSQL Server!
echo Template Service: http://localhost:8081
echo Gateway Service: http://localhost:8082
echo Database: templatedb on localhost

pause