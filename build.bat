@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   API Pressure Test Tool - Build Script
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [1/5] Installing root dependencies...
cd /d "%~dp0"
call npm install
if errorlevel 1 (
    echo Root dependency install failed
    pause
    exit /b 1
)

echo.
echo [2/5] Installing Electron dependencies...
cd /d "%~dp0electron"
call npm install
if errorlevel 1 (
    echo Electron dependency install failed
    pause
    exit /b 1
)

echo.
echo [3/5] Building frontend...
cd /d "%~dp0frontend"
call npm run build
if errorlevel 1 (
    echo Frontend build failed
    pause
    exit /b 1
)

echo.
echo [4/5] Building backend...
cd /d "%~dp0backend"
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
    echo Backend build failed
    pause
    exit /b 1
)

echo.
echo [5/5] Packaging Electron app...
cd /d "%~dp0"
call npx electron-builder --win
if errorlevel 1 (
    echo Packaging failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Build complete!
echo   Output: release/
echo ========================================
pause
