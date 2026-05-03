# Booby Client Auto-Update (GitHub Pages)

This launcher checks GitHub Pages for update.json and forces an update if a newer version is available.

## 1) Enable GitHub Pages

- Repo: XriuxDev/BoobyClient
- Pages source: deploy from the default branch root (or use /docs if you prefer).
- The URL should be:
  https://xriuxdev.github.io/BoobyClient/

## 2) Publish update.json + installer

Place these files at the Pages root:
- update.json
- BoobyLauncherSetup-1.0.0.exe (or newer)

Example update.json:
{
  "version": "1.0.0",
  "installerUrl": "https://xriuxdev.github.io/BoobyClient/BoobyLauncherSetup-1.0.0.exe",
  "sha256": "",
  "notes": "Initial public release"
}

## 3) Version bumps

When you release a new version:
1. Build new installer.
2. Upload it to the Pages root.
3. Update update.json version and installerUrl.
4. Commit and push to update GitHub Pages.

## 4) Forced update behavior

- If update.json reports a newer version, the launcher downloads the installer and exits.
- If update.json cannot be fetched, the launcher continues (no forced block).
- If update is required but download fails, the launcher exits.
