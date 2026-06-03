@echo off
echo ========================================
echo   API压测工具 - 开发模式启动
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo [1/3] 安装前端依赖...
cd /d "%~dp0frontend"
call npm install
if errorlevel 1 (
    echo 前端依赖安装失败
    pause
    exit /b 1
)

echo.
echo [2/3] 安装根目录依赖...
cd /d "%~dp0"
call npm install
if errorlevel 1 (
    echo 根目录依赖安装失败
    pause
    exit /b 1
)

echo.
echo [3/3] 启动开发服务器...
echo 前端: http://localhost:3000
echo 后端: http://localhost:8080
echo.
echo 按 Ctrl+C 停止服务器
echo.

call npm run dev
pause
