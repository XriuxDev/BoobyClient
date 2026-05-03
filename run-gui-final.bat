@echo off
REM Booby Client - Modern GUI Launcher (Feather Client Style)

cd /d "%~dp0"

echo Starting Booby Client GUI Launcher...
echo.

REM Launch with JavaFX 21.0.11
java --module-path "C:\javafx-sdk-25\javafx-sdk-21.0.11\lib" ^
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web ^
     -cp "build/classes;lib/slf4j-api-2.0.9.jar;lib/slf4j-simple-2.0.9.jar;lib/gson-2.10.1.jar" ^
     com.boobyclient.launcher.ModernLauncherApp

if errorlevel 1 (
    echo.
    echo Failed to start GUI launcher.
    pause
)
