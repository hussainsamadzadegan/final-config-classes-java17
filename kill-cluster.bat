@echo off
echo ================================================
echo    Killing Config Cluster Nodes (8081, 8082, 8083)
echo ================================================

echo Killing processes on port 8081...
for /f "tokens=5 delims= " %%a in ('netstat -aon ^| find ":8081"') do taskkill /F /PID %%a 2>nul

echo Killing processes on port 8082...
for /f "tokens=5 delims= " %%a in ('netstat -aon ^| find ":8082"') do taskkill /F /PID %%a 2>nul

echo Killing processes on port 8083...
for /f "tokens=5 delims= " %%a in ('netstat -aon ^| find ":8083"') do taskkill /F /PID %%a 2>nul

echo.
echo Done! All processes on ports 8081, 8082, 8083 have been terminated.
echo.
pause