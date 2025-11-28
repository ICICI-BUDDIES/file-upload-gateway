@echo off
echo ========================================
echo    PRODUCTION VALIDATION CHECKLIST
echo ========================================

echo.
echo ✅ Checking project structure...
if exist "backend\gateway-service\pom.xml" (
    echo    ✅ Gateway service found
) else (
    echo    ❌ Gateway service missing
    goto :failed
)

if exist "backend\template-service\pom.xml" (
    echo    ✅ Template service found
) else (
    echo    ❌ Template service missing
    goto :failed
)

echo.
echo ✅ Checking configuration files...
if exist "backend\template-service\src\main\resources\application-prod.yml" (
    echo    ✅ Production config found
) else (
    echo    ❌ Production config missing
    goto :failed
)

echo.
echo ✅ Checking database setup...
if exist "backend\template-service\database-setup.sql" (
    echo    ✅ Database setup script found
) else (
    echo    ❌ Database setup script missing
    goto :failed
)

echo.
echo ✅ Checking build artifacts...
call mvn -f backend\gateway-service\pom.xml clean compile -q
if %ERRORLEVEL% equ 0 (
    echo    ✅ Gateway service compiles successfully
) else (
    echo    ❌ Gateway service compilation failed
    goto :failed
)

call mvn -f backend\template-service\pom.xml clean compile -q
if %ERRORLEVEL% equ 0 (
    echo    ✅ Template service compiles successfully
) else (
    echo    ❌ Template service compilation failed
    goto :failed
)

echo.
echo ✅ Running tests...
call mvn -f backend\gateway-service\pom.xml test -q
if %ERRORLEVEL% equ 0 (
    echo    ✅ Gateway service tests pass
) else (
    echo    ❌ Gateway service tests failed
    goto :failed
)

call mvn -f backend\template-service\pom.xml test -q
if %ERRORLEVEL% equ 0 (
    echo    ✅ Template service tests pass
) else (
    echo    ❌ Template service tests failed
    goto :failed
)

echo.
echo ✅ Checking for production issues...
findstr /r /c:"System\.out\.println" backend\gateway-service\src\main\java\com\example\gateway\*.java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo    ⚠️  Warning: Debug prints found in gateway service
) else (
    echo    ✅ No debug prints in gateway service
)

findstr /r /c:"TODO\|FIXME\|XXX" backend\*\src\main\java\*.java >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo    ⚠️  Warning: TODO/FIXME comments found
) else (
    echo    ✅ No TODO/FIXME comments found
)

echo.
echo ========================================
echo    🎉 PRODUCTION VALIDATION PASSED!
echo ========================================
echo.
echo Your project is ready for production deployment:
echo.
echo 📦 Build Command: build-all.bat
echo 🚀 Deploy Files: 
echo    - gateway-service-0.0.1-SNAPSHOT.jar
echo    - template-service-1.0.0.jar
echo 🗄️  Database: Run database-setup.sql on MSSQL
echo ⚙️  Environment: Set SPRING_PROFILES_ACTIVE=prod
echo.
echo Next steps:
echo 1. Run production-test.bat to simulate production
echo 2. Open production-load-test.html to test load
echo 3. Deploy to production servers
echo.
goto :end

:failed
echo.
echo ========================================
echo    ❌ PRODUCTION VALIDATION FAILED!
echo ========================================
echo.
echo Please fix the issues above before deploying to production.
echo.

:end
pause