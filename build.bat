@echo off
cd /d "%~dp0"

if not exist build\classes mkdir build\classes

javac --module-path "C:\javafx-sdk-25\javafx-sdk-21.0.11\lib" ^
      --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
      -encoding UTF-8 ^
      -cp "build\classes;lib\slf4j-api-2.0.9.jar;lib\gson-2.10.1.jar" ^
      -d build\classes ^
      src\main\java\com\boobyclient\launcher\ModernLauncherApp.java ^
      src\main\java\com\boobyclient\launcher\ModernLauncherController.java ^
      src\main\java\com\boobyclient\launcher\MinecraftAuthManager.java ^
      src\main\java\com\boobyclient\launcher\OAuthManager.java ^
      src\main\java\com\boobyclient\launcher\SocketServer.java ^
      src\main\java\com\boobyclient\util\GameLauncher.java ^
      src\main\java\com\boobyclient\util\GameInstaller.java ^
      src\main\java\com\boobyclient\util\FabricInjector.java

if errorlevel 1 (
    echo.
    echo BUILD FAILED.
    pause
    exit /b 1
)

echo Copying resources...
xcopy /E /I /Y src\main\resources\* build\classes\

echo Building Fabric mod...
pushd booby-mod
call gradlew build
popd

echo Copying mod to Minecraft...
if not exist "%APPDATA%\.minecraft\mods" mkdir "%APPDATA%\.minecraft\mods"
if exist "booby-mod\build\libs\booby-mod-1.0.0.jar" copy /Y "booby-mod\build\libs\booby-mod-1.0.0.jar" "%APPDATA%\.minecraft\mods\booby-client.jar"


echo.
echo BUILD SUCCESSFUL.
