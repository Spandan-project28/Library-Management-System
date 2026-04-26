@echo off
setlocal

set SRC=src
set OUT=out
set MAIN=Main

echo ============================================
echo   Smart Library System - Build ^& Run
echo ============================================

:: Check for Java
where javac >nul 2>&1
if errorlevel 1 (
    echo [ERROR] javac not found. Please install JDK 17+ and add it to PATH.
    pause & exit /b 1
)

:: Create output directory
if not exist "%OUT%" mkdir "%OUT%"

echo [1/2] Compiling sources...
dir /s /b "%SRC%\*.java" > sources.txt
javac -d "%OUT%" @sources.txt
del sources.txt

if errorlevel 1 (
    echo [ERROR] Compilation failed.
    pause & exit /b 1
)

echo [2/2] Running application...
java -cp "%OUT%" %MAIN%

endlocal
