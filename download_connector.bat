@echo off
echo ========================================================
echo Downloading MySQL Connector/J JDBC Driver (v8.3.0)
echo ========================================================

if not exist lib mkdir lib

powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar', 'lib\mysql-connector-j-8.3.0.jar')"

if exist "lib\mysql-connector-j-8.3.0.jar" (
    echo [SUCCESS] mysql-connector-j-8.3.0.jar downloaded into lib/
) else (
    echo [ERROR] Failed to download. Please manually place mysql-connector-j-*.jar into the lib folder.
)
pause
