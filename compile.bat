@echo off
echo ========================================================
echo Compiling Student Placement Management System (Java 17+)
echo ========================================================

if not exist bin mkdir bin

REM Compile all java source files
javac -d bin -sourcepath src src\com\placement\main\MainApp.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation successful!
    REM Copy resources/db.properties to bin for classpath discovery
    copy /Y resources\db.properties bin\db.properties >nul
    echo [INFO] Copied db.properties into bin/
) else (
    echo [FAILED] Compilation encountered errors.
)
pause
