@echo off
echo Running tests for both microservices...

echo.
echo Testing Gateway Service...
cd backend\gateway-service
call mvn test
if %ERRORLEVEL% neq 0 (
    echo Gateway Service tests failed!
    exit /b 1
)

echo.
echo Testing Template Service...
cd ..\template-service
call mvn test
if %ERRORLEVEL% neq 0 (
    echo Template Service tests failed!
    exit /b 1
)

echo.
echo All tests passed successfully!
pause