@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   API Pressure Test Tool - Dev Mode
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [1/3] Installing frontend dependencies...
cd /d "%~dp0frontend"
call npm install
if errorlevel 1 (
    echo Frontend dependency install failed
    pause
    exit /b 1
)

echo.
echo [2/3] Installing root dependencies...
cd /d "%~dp0"
call npm install
if errorlevel 1 (
    echo Root dependency install failed
    pause
    exit /b 1
)

echo.
echo [3/3] Starting dev server...
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080
echo.
echo Press Ctrl+C to stop
echo.

call npm run dev
pause
