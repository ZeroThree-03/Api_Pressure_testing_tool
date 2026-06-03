@echo off
echo ========================================
echo   API压测工具 - 打包脚本
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [1/4] 安装根目录依赖...
cd /d "%~dp0"
call npm install
if errorlevel 1 (
    echo 依赖安装失败
    pause
    exit /b 1
)

echo.
echo [2/4] 安装Electron依赖...
cd /d "%~dp0electron"
call npm install
if errorlevel 1 (
    echo Electron依赖安装失败
    pause
    exit /b 1
)

echo.
echo [3/4] 构建前端...
cd /d "%~dp0frontend"
call npm run build
if errorlevel 1 (
    echo 前端构建失败
    pause
    exit /b 1
)

echo.
echo [4/4] 打包Electron应用...
cd /d "%~dp0"
call npx electron-builder --win
if errorlevel 1 (
    echo 打包失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo   打包完成！
echo   输出目录: release/
echo ========================================
pause
