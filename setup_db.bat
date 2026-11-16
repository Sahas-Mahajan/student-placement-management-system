@echo off
echo ========================================================
echo Setting up MySQL Database for Placement System
echo ========================================================

set /p MYSQL_USER=Enter MySQL Username (default: root): 
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo Importing sql/schema.sql into MySQL...
mysql -u %MYSQL_USER% -p < sql\schema.sql

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Database 'placement_db' and tables created with seed data!
) else (
    echo [ERROR] Failed to run schema.sql. Ensure MySQL server is running.
)
pause
