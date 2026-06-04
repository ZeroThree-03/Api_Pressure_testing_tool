@echo off
chcp 65001 >nul 2>&1
echo ========================================
echo   API Pressure Test Tool - Backend Start
echo ========================================

set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo.
echo Compiling and starting backend...
cd /d "%~dp0backend"

echo Java version:
java -version
echo.

echo Downloading Maven Wrapper...
if not exist "mvnw" (
    curl -L -o mvnw "https://raw.githubusercontent.com/apache/maven-wrapper/master/maven-wrapper-distribution/src/resources/mvnw"
    curl -L -o mvnw.cmd "https://raw.githubusercontent.com/apache/maven-wrapper/master/maven-wrapper-distribution/src/resources/mvnw.cmd"
)

echo.
echo Compiling project...
call mvnw.cmd clean compile -q
if errorlevel 1 (
    echo Compile failed
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot...
call mvnw.cmd spring-boot:run
pause
