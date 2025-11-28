@echo off
REM Production Deployment Script for Windows

REM Set environment variables
set SPRING_PROFILES_ACTIVE=prod
set DB_HOST=your-mssql-server-ip
set DB_PORT=1433
set DB_NAME=templatedb
set DB_USERNAME=sa
set DB_PASSWORD=your-database-password

REM Create storage directory
if not exist "storage" mkdir storage

echo Starting Template Service on port 8081...
start "Template Service" java -jar template-service-1.0.0.jar

REM Wait for Template Service to start
timeout /t 30 /nobreak

echo Starting Gateway Service on port 8080...
start "Gateway Service" java -jar gateway-service-0.0.1-SNAPSHOT.jar

echo Both services started successfully!
echo Template Service: http://localhost:8081
echo Gateway Service: http://localhost:8080
pause