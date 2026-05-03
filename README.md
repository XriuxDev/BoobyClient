# Booby Client

Booby Client is a JavaFX Minecraft launcher with lightweight PvP HUD features.

## Features

- Toggle Sprint
- FPS Counter
- Ping Display
- Combo Counter
- Version selector and profiles

## Build (Windows)

```bat
build-exe.bat
```

Output:
```
dist\installer\BoobyLauncherSetup-1.0.0.exe
```

## Auto-update (GitHub Pages)

The launcher checks a public update manifest and forces updates when a newer version is available.

Publish these files to the GitHub Pages root:
- update.json
- BoobyLauncherSetup-1.0.0.exe (or newer)

Current update URL:
```
https://xriuxdev.github.io/BoobyClient/update.json
```

### Release a new version

1. Build the new installer.
2. Upload the new installer to GitHub Pages.
3. Update version and installerUrl in update.json.
4. Bump CURRENT_VERSION in UpdateManager.

## Notes

- The launcher is meant for vanilla-legal gameplay.
- JavaFX WebView is required for Microsoft sign-in.
