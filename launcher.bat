@echo off
REM Booby Client - Launcher Selector

cls

echo.
echo ╔═════════════════════════════════════╗
echo ║     BOOBY CLIENT - Launcher         ║
echo ║       Minecraft 1.21+ PvP           ║
echo ╚═════════════════════════════════════╝
echo.
echo Choose which launcher to run:
echo.
echo 1. CLI Launcher (Command-line interface)
echo 2. GUI Launcher (Visual interface - Feather Client style)
echo 3. Exit
echo.
set /p choice="Enter your choice (1-3): "

if "%choice%"=="1" (
    goto cli
) else if "%choice%"=="2" (
    goto gui
) else if "%choice%"=="3" (
    exit /b 0
) else (
    echo Invalid choice. Please try again.
    timeout /t 2 >nul
    goto start
)

:cli
cls
echo Starting Booby Client CLI Launcher...
echo.
cd /d "%~dp0"
java -cp "booby-client.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-simple-2.0.9.jar;lib/gson-2.10.1.jar" com.boobyclient.launcher.CLILauncher
goto end

:gui
cls
echo Starting Booby Client GUI Launcher...
echo.
echo NOTE: The GUI launcher requires JavaFX to be set up.
echo If this doesn't work, please install JavaFX SDK.
echo.
cd /d "%~dp0"
java -cp "booby-client.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-simple-2.0.9.jar;lib/gson-2.10.1.jar" com.boobyclient.launcher.ModernLauncherApp
goto end

:end
if errorlevel 1 (
    echo.
    echo Error occurred. Press any key to return to menu...
    pause >nul
    cls
    goto start
)
