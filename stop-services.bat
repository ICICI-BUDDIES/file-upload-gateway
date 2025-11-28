@echo off
REM Stop all services

echo Stopping Gateway Service...
for /f "tokens=2" %%i in ('tasklist /fi "windowtitle eq Gateway Service*" /fo table /nh') do taskkill /pid %%i /f

echo Stopping Template Service...
for /f "tokens=2" %%i in ('tasklist /fi "windowtitle eq Template Service*" /fo table /nh') do taskkill /pid %%i /f

echo All services stopped.
pause