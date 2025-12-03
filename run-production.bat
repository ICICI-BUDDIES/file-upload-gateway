@echo off
echo Starting Production Mode with MSSQL...

set SPRING_PROFILES_ACTIVE=prod

echo Starting Template Service (Port 8081)...
start "Template Service" java -jar backend\template-service\target\template-service-1.0.0.jar

timeout /t 20 /nobreak

echo Starting Gateway Service (Port 8082)...
start "Gateway Service" java -jar backend\gateway-service\target\gateway-service-0.0.1-SNAPSHOT.jar

echo Production services started!
echo Template Service: http://localhost:8081
echo Gateway Service: http://localhost:8082
echo Database: master on localhost (myuser/mypassword)

pause