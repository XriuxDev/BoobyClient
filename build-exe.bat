@echo off
setlocal

cd /d "%~dp0"

set APP_NAME=Booby Client

:: Extract version from ModernLauncherApp.java
for /f "tokens=7" %%a in ('findstr /C:"public static final String VERSION =" src\main\java\com\boobyclient\launcher\ModernLauncherApp.java') do set APP_VERSION_RAW=%%a
:: Clean up quotes and semicolon
set APP_VERSION=%APP_VERSION_RAW:"=%
set APP_VERSION=%APP_VERSION:;=%

echo Building version: %APP_VERSION%

set MAIN_CLASS=com.boobyclient.launcher.Main
set JAVA_FX_LIB=C:\javafx-sdk-25\javafx-sdk-21.0.11\lib
set JAVA_FX_BIN=C:\javafx-sdk-25\javafx-sdk-21.0.11\bin
set DIST_DIR=dist2
set APP_DIR=%DIST_DIR%\app
set INSTALLER_DIR=%DIST_DIR%\installer
set "WIX_BIN=C:\Program Files (x86)\WiX Toolset v3.14\bin"
set "OUTPUT_EXE=BoobyLauncherSetup-%APP_VERSION%.exe"

where jpackage >nul 2>nul
if errorlevel 1 (
    echo jpackage not found. Please install JDK 17+ and ensure it is on PATH.
    exit /b 1
)

where candle >nul 2>nul
if errorlevel 1 (
    if exist "%WIX_BIN%\candle.exe" (
        set "PATH=%WIX_BIN%;%PATH%"
    ) else (
        echo WiX not found. Ensure candle.exe and light.exe are on PATH.
        exit /b 1
    )
)

where light >nul 2>nul
if errorlevel 1 (
    if exist "%WIX_BIN%\light.exe" (
        set "PATH=%WIX_BIN%;%PATH%"
    ) else (
        echo WiX not found. Ensure candle.exe and light.exe are on PATH.
        exit /b 1
    )
)

if not exist "%JAVA_FX_LIB%" (
    echo JavaFX SDK not found at %JAVA_FX_LIB%.
    echo Update JAVA_FX_LIB in build-exe.bat to your JavaFX SDK lib folder.
    exit /b 1
)

if exist "%DIST_DIR%" rmdir /S /Q "%DIST_DIR%"
if exist "%DIST_DIR%" (
    echo Failed to clean %DIST_DIR%. Close any Explorer window in dist and try again.
    exit /b 1
)
if exist build\classes rmdir /S /Q build\classes
if exist build\classes (
    echo Failed to clean build\classes. Close any running build and try again.
    exit /b 1
)
mkdir build\classes
mkdir "%APP_DIR%"
mkdir "%APP_DIR%\javafx"
mkdir "%INSTALLER_DIR%"

echo Compiling sources...
dir /s /b src\main\java\*.java > sources.txt
javac --module-path "%JAVA_FX_LIB%" ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web ^
      -encoding UTF-8 ^
      -cp "lib\*" ^
      -d build\classes @sources.txt

if errorlevel 1 (
    echo Compilation failed.
    del sources.txt
    exit /b 1
)

del sources.txt

echo Copying resources...
xcopy /E /I /Y src\main\resources\* build\classes\ >nul

echo Creating app JAR...
jar cfe "%APP_DIR%\booby-launcher.jar" %MAIN_CLASS% -C build\classes .

if errorlevel 1 (
    echo JAR creation failed.
    exit /b 1
)

echo Copying dependencies...
xcopy /Y lib\*.jar "%APP_DIR%\" >nul

echo Copying JavaFX libraries...
xcopy /Y "%JAVA_FX_LIB%\*.jar" "%APP_DIR%\javafx\" >nul

echo Copying JavaFX native libraries...
if exist "%JAVA_FX_BIN%" (
    xcopy /Y "%JAVA_FX_BIN%\*.dll" "%APP_DIR%\" >nul
) else (
    echo Warning: JavaFX bin folder not found at %JAVA_FX_BIN%. Native libs might be missing.
)

echo Building installer...
set LOG_FILE=%DIST_DIR%\jpackage.log
if exist "%LOG_FILE%" del /F /Q "%LOG_FILE%"

set "ICON_FLAG="
if exist "icon.ico" set ICON_FLAG=--icon "icon.ico"

jpackage --verbose ^
    --type exe ^
    --name "%APP_NAME%" ^
    --app-version "%APP_VERSION%" ^
    --input "%APP_DIR%" ^
    --main-jar booby-launcher.jar ^
    --main-class %MAIN_CLASS% ^
    --java-options "--module-path=$APPDIR\\javafx" ^
    --java-options "--add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.web" ^
    %ICON_FLAG% ^
    --dest "%INSTALLER_DIR%" ^
    --vendor "XriuxDev" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "Booby Client" > "%LOG_FILE%" 2>&1

if errorlevel 1 (
    echo jpackage failed.
    echo See %LOG_FILE% for details.
    if exist "%LOG_FILE%" type "%LOG_FILE%"
    exit /b 1
)

echo.
echo Installer created in %INSTALLER_DIR%.
echo Moving installer to project root...

if exist "%OUTPUT_EXE%" del /F /Q "%OUTPUT_EXE%"
move /Y "%INSTALLER_DIR%\%APP_NAME%-%APP_VERSION%.exe" "%OUTPUT_EXE%" >nul

echo.
echo SUCCESS: Installer is at: %~dp0%OUTPUT_EXE%
echo.
echo Upload this file to GitHub and update update.json to enable auto-updates.
endlocal

