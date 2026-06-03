@echo off
echo ========================================
echo   API压测工具 - 后端启动
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo 正在编译并启动后端...
cd /d "%~dp0backend"

echo 使用Java版本:
java -version
echo.

echo 正在下载Maven Wrapper...
if not exist "mvnw" (
    curl -L -o mvnw "https://raw.githubusercontent.com/apache/maven-wrapper/master/maven-wrapper-distribution/src/resources/mvnw"
    curl -L -o mvnw.cmd "https://raw.githubusercontent.com/apache/maven-wrapper/master/maven-wrapper-distribution/src/resources/mvnw.cmd"
)

echo.
echo 正在编译项目...
call mvnw.cmd clean compile -q
if errorlevel 1 (
    echo 编译失败
    pause
    exit /b 1
)

echo.
echo 正在启动Spring Boot...
call mvnw.cmd spring-boot:run
pause
