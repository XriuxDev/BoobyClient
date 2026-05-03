# Booby Client - GUI Setup Guide

## Overview

The Booby Client has **two launcher options**:

1. **CLI Launcher** ✅ - Works now, command-line interface
2. **GUI Launcher** 🎨 - Modern visual interface (Feather Client style) - Requires JavaFX setup

---

## Quick Start

### Run Immediately (CLI)
```batch
launcher.bat
```
Then choose option **1** for CLI Launcher

### GUI Launcher (Requires Setup Below)

---

## GUI Setup Instructions

The GUI launcher is built with **JavaFX**, which requires additional setup. Here's how to get it working:

### Option 1: Install JavaFX SDK (Recommended)

#### Windows:
1. Download JavaFX SDK from: https://gluonhq.com/products/javafx/
2. Extract to `C:\javafx-sdk-21.0.2` (or similar)
3. Add to your PATH or create a `JAVAFX_HOME` environment variable
4. Run:
```batch
java --module-path "C:\javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp "booby-client.jar;lib/*" com.boobyclient.launcher.ModernLauncherApp
```

#### Linux:
```bash
# Install JavaFX
sudo apt install openjfx

# Run launcher
java --module-path /usr/share/openjfx --add-modules javafx.controls,javafx.fxml -cp "booby-client.jar:lib/*" com.boobyclient.launcher.ModernLauncherApp
```

#### macOS:
```bash
# Using Homebrew
brew install javafx

# Run launcher
java --module-path /opt/homebrew/Cellar/javafx-sdk/21.0.2/lib --add-modules javafx.controls,javafx.fxml -cp "booby-client.jar:lib/*" com.boobyclient.launcher.ModernLauncherApp
```

### Option 2: Use Maven (Automatic Setup)

If you have Maven installed:
```bash
mvn clean javafx:run -Dcom.sun.javafx.version=21.0.2
```

### Option 3: Use Gradle

Create a `build.gradle` file in the project root:
```gradle
plugins {
    id 'java'
    id 'org.openjfx.javafxplugin' version '0.0.13'
}

javafx {
    version = "21.0.2"
    modules = [ 'javafx.controls', 'javafx.fxml' ]
}

dependencies {
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'org.slf4j:slf4j-api:2.0.9'
    implementation 'org.slf4j:slf4j-simple:2.0.9'
}
```

Then run:
```bash
gradle build
gradle run
```

---

## GUI Features

### 🏠 Home Screen
- **Quick Launch** section with version and account selection
- **Active Mods** - Toggle HUD modules on/off
- **Launch Game** button
- **Client Info** panel with version and features

### 🎨 HUD Screen
- **FPS Counter** - Toggle and scale
- **Ping Display** - Toggle and scale
- **Toggle Sprint** - Toggle and scale
- **Combo Counter** - Toggle and scale
- Save configuration button

### ⚙️ Settings Screen
- Java memory allocation (2G, 4G, 6G, 8G)
- Theme selection (Dark, Light, Hacker)
- Auto-launch option
- Keep launcher open after launch
- Save settings button

### 📁 Profiles Screen
- Create new profiles
- Load saved profiles
- Delete profiles
- Profile list management

---

## GUI Architecture

```
ModernLauncherApp.java
├── FXML: modern-launcher.fxml
│   ├── Home Screen (HomeController)
│   ├── HUD Screen (HUDController)
│   ├── Settings Screen (SettingsController)
│   └── Profiles Screen (ProfilesController)
└── CSS: modern-style.css
    ├── Dark theme
    ├── Green accent colors (#00ff00)
    ├── Professional gaming aesthetic
    └── Smooth interactions
```

---

## Design Inspiration: Feather Client

The GUI is inspired by **Feather Client** with:
- ✅ Clean dark theme
- ✅ Green accent color (#00ff00)
- ✅ Multiple tabs/screens
- ✅ Professional layout
- ✅ Quick access to settings
- ✅ Modern button styling
- ✅ Responsive controls

---

## Troubleshooting

### Issue: JavaFX classes not found
**Solution**: Make sure JavaFX SDK is installed and module path is set correctly

### Issue: "error: package javafx.application does not exist"
**Solution**: Download JavaFX SDK from Gluon (official source)

### Issue: Launcher won't open in GUI mode
**Solution**: Fall back to CLI launcher until JavaFX is properly installed

---

## Easy GUI Launcher Script

Save as `run-gui-easy.bat` (for after JavaFX is set up):

```batch
@echo off
setlocal enabledelayedexpansion

REM Path to JavaFX SDK (modify as needed)
set JAVAFX_SDK=C:\javafx-sdk-21.0.2

if not exist "%JAVAFX_SDK%" (
    echo JavaFX SDK not found at %JAVAFX_SDK%
    echo Please install JavaFX or update the path in this script
    pause
    exit /b 1
)

java --module-path "%JAVAFX_SDK%\lib" --add-modules javafx.controls,javafx.fxml ^
     -cp "booby-client.jar;lib/slf4j-api-2.0.9.jar;lib/slf4j-simple-2.0.9.jar;lib/gson-2.10.1.jar" ^
     com.boobyclient.launcher.ModernLauncherApp

pause
```

---

## Source Code Structure

The GUI is implemented in:

- **ModernLauncherApp.java** - Application entry point
- **ModernLauncherController.java** - Main UI controller with event handlers
- **modern-launcher.fxml** - UI definition (layouts, buttons, inputs)
- **modern-style.css** - Professional dark theme styling

---

## Next Steps

### For Development:
1. Download JavaFX SDK
2. Add to your IDE (IntelliJ IDEA, Eclipse, VS Code)
3. Set module path in run configuration
4. Run `ModernLauncherApp.java`

### For Users:
1. Use CLI launcher for immediate usage
2. Follow Setup Instructions above for GUI
3. Share your favorite profile settings!

---

## Current Status

| Launcher | Status | Requirements |
|----------|--------|--------------|
| CLI | ✅ Ready | Java 17+ |
| GUI | 🎨 Built | Java 17+ + JavaFX SDK |

---

**Note**: The GUI code is complete and ready. It just needs JavaFX SDK installed on your system to run. The CLI launcher works immediately with zero setup!

Good luck! 🚀
