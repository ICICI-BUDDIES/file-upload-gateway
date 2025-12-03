@echo off
echo Testing SQL Server Connection...
echo.
sqlcmd -S localhost,1433 -d myappdb -U myuser -P mypassword -Q "SELECT @@VERSION; SELECT DB_NAME();"
echo.
if %ERRORLEVEL% EQU 0 (
    echo SUCCESS: Database connection works!
) else (
    echo FAILED: Cannot connect to database
)
pause
