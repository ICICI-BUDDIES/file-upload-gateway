@echo off
echo ========================================
echo    PRODUCTION SIMULATION TEST
echo ========================================

echo.
echo 1. Building both services for production...
call build-all.bat
if %ERRORLEVEL% neq 0 (
    echo ❌ Build failed - not production ready!
    pause
    exit /b 1
)

echo.
echo 2. Setting production environment variables...
set SPRING_PROFILES_ACTIVE=prod
set DB_HOST=localhost
set DB_PORT=1433
set DB_NAME=templatedb
set DB_USERNAME=sa
set DB_PASSWORD=TestPassword123!
set SERVER_PORT=8081

echo ✅ Environment configured for production simulation

echo.
echo 3. Starting Template Service (Production Mode)...
echo Starting on port 8081 with MSSQL configuration...
start "Template Service" cmd /k "cd backend\template-service && java -jar target\template-service-1.0.0.jar"

echo.
echo 4. Waiting for Template Service to start...
timeout /t 15 /nobreak

echo.
echo 5. Starting Gateway Service (Production Mode)...
echo Starting on port 8080...
start "Gateway Service" cmd /k "cd backend\gateway-service && java -jar target\gateway-service-0.0.1-SNAPSHOT.jar"

echo.
echo 6. Waiting for Gateway Service to start...
timeout /t 15 /nobreak

echo.
echo ========================================
echo    PRODUCTION HEALTH CHECKS
echo ========================================

echo.
echo Testing Template Service health...
curl -f http://localhost:8081/actuator/health 2>nul
if %ERRORLEVEL% equ 0 (
    echo ✅ Template Service is healthy
) else (
    echo ❌ Template Service health check failed
)

echo.
echo Testing Gateway Service health...
curl -f http://localhost:8080/api/test 2>nul
if %ERRORLEVEL% equ 0 (
    echo ✅ Gateway Service is healthy
) else (
    echo ❌ Gateway Service health check failed
)

echo.
echo ========================================
echo    PRODUCTION SIMULATION COMPLETE
echo ========================================
echo.
echo Services are running in production mode:
echo - Template Service: http://localhost:8081
echo - Gateway Service: http://localhost:8080
echo.
echo Press any key to stop services...
pause

echo.
echo Stopping services...
taskkill /f /im java.exe 2>nul
echo Services stopped.
pause