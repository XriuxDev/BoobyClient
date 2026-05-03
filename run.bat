@echo off
REM Booby Client Launcher - Run Script

cd /d "%~dp0"

echo Starting Booby Client...
echo.

java -cp "booby-client.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-simple-2.0.9.jar;lib/gson-2.10.1.jar" com.boobyclient.launcher.CLILauncher

pause
