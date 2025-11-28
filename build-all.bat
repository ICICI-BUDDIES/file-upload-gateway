@echo off
echo Building both microservices with tests...

echo.
echo Building Gateway Service...
cd backend\gateway-service
call mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Gateway Service build failed!
    exit /b 1
)

echo.
echo Building Template Service...
cd ..\template-service
call mvn clean package
if %ERRORLEVEL% neq 0 (
    echo Template Service build failed!
    exit /b 1
)

echo.
echo Build completed successfully!
echo.
echo JAR files created:
echo - Gateway Service: backend\gateway-service\target\gateway-service-0.0.1-SNAPSHOT.jar
echo - Template Service: backend\template-service\target\template-service-1.0.0.jar
echo.
pause