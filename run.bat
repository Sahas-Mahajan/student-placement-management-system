@echo off
setlocal enabledelayedexpansion
echo ========================================================
echo Launching Student Placement Management System
echo ========================================================

REM Check if bin directory exists
if not exist "bin\com\placement\main\MainApp.class" (
    echo [ERROR] Compiled classes not found. Running compile.bat first...
    call compile.bat
)

REM Build classpath including bin and all JARs in lib directory
set CP=bin;resources
if exist "lib" (
    for %%i in (lib\*.jar) do (
        set CP=!CP!;%%i
    )
)

echo Classpath: %CP%
echo Starting Application...
echo.

java -cp "%CP%" com.placement.main.MainApp

endlocal
pause
