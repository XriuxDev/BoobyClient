@echo off
REM Booby Client - Modern GUI Launcher (Feather Client Style)

cd /d "%~dp0"

echo Starting Booby Client Modern Launcher...
echo.

REM Launch with GUI
java -cp "booby-client.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-simple-2.0.9.jar;lib/gson-2.10.1.jar" com.boobyclient.launcher.ModernLauncherApp

if errorlevel 1 (
    echo.
    echo Failed to start launcher. Make sure Java 17+ is installed.
    pause
)
