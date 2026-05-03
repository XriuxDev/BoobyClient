# Booby Client - GUI Visual Preview

## Launcher Design Overview

This is what the modern GUI launcher looks like (Feather Client style):

```
╔════════════════════════════════════════════════════════════════════════════╗
║ BOOBY CLIENT  │  Home  │  Settings  │  HUD  │  Profiles                   ║
╠════════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║  ┌─────────────────────────────────┐  ┌────────────────────────────────┐ ║
║  │  QUICK LAUNCH                   │  │  CLIENT INFO                   │ ║
║  ├─────────────────────────────────┤  ├────────────────────────────────┤ ║
║  │                                 │  │                                │ ║
║  │  Version:                       │  │  Version: 1.0.0                │ ║
║  │  [1.21 ▼]                       │  │  Target: Minecraft 1.21+       │ ║
║  │                                 │  │                                │ ║
║  │  Account:                       │  │  ════════════════════════════  │ ║
║  │  [Player1 ▼]                    │  │                                │ ║
║  │                                 │  │  FEATURES                      │ ║
║  │  ─────────────────────────────  │  │  ✓ Toggle Sprint Fix           │ ║
║  │  ACTIVE MODS                    │  │  ✓ FPS Counter                 │ ║
║  │  ☑ Toggle Sprint                │  │  ✓ Ping Display                │ ║
║  │  ☑ FPS Counter                  │  │  ✓ Combo Counter               │ ║
║  │  ☑ Ping Display                 │  │  ✓ Profile Manager             │ ║
║  │  ☑ Combo Counter                │  │                                │ ║
║  │                                 │  │  ════════════════════════════  │ ║
║  │                                 │  │                                │ ║
║  │  ┌─────────────────────────────┐│  │  VANILLA LEGAL ⚠               │ ║
║  │  │  LAUNCH GAME                ││  │  No cheating mods              │ ║
║  │  └─────────────────────────────┘│  │  No unfair advantages          │ ║
║  │                                 │  │  Server compliant              │ ║
║  │  Ready to launch                │  │                                │ ║
║  │  ████████░░░░░░░░░░ 50%         │  │  Made with ❤ for PvP           │ ║
║  │                                 │  │                                │ ║
║  └─────────────────────────────────┘  └────────────────────────────────┘ ║
║                                                                            ║
╚════════════════════════════════════════════════════════════════════════════╝
```

---

## Screen 1: Home (Main Launch Screen)

### Left Panel - Quick Launch
```
╔════════════════════════════════════╗
║ QUICK LAUNCH                       ║
╠════════════════════════════════════╣
║                                    ║
║ Version:                           ║
║ ┌────────────────────────────────┐ ║
║ │ 1.21                        ▼ │ ║
║ └────────────────────────────────┘ ║
║                                    ║
║ Account:                           ║
║ ┌────────────────────────────────┐ ║
║ │ Player1                     ▼ │ ║
║ └────────────────────────────────┘ ║
║                                    ║
║ ─────────────────────────────────  ║
║ ACTIVE MODS                        ║
║ ☑ Toggle Sprint                    ║
║ ☑ FPS Counter                      ║
║ ☑ Ping Display                     ║
║ ☑ Combo Counter                    ║
║                                    ║
║ ┌────────────────────────────────┐ ║
║ │    LAUNCH GAME                 │ ║  Green button, large
║ └────────────────────────────────┘ ║
║ Ready to launch                    ║
║ ████░░░░░░░░░░░░░░ 20%             ║
║                                    ║
╚════════════════════════════════════╝
```

### Right Panel - Client Info
```
╔════════════════════════════════════╗
║ CLIENT INFO                        ║
╠════════════════════════════════════╣
║                                    ║
║ Version        │ 1.0.0             ║
║ Target         │ Minecraft 1.21+   ║
║                                    ║
║ ════════════════════════════════   ║
║                                    ║
║ FEATURES                           ║
║ ✓ Toggle Sprint (vanilla fix)      ║
║ ✓ FPS Counter (color coded)        ║
║ ✓ Ping Display (real-time)         ║
║ ✓ Combo Counter (PvP)              ║
║ ✓ Profile Manager                  ║
║                                    ║
║ ════════════════════════════════   ║
║                                    ║
║ VANILLA LEGAL ⚠️                    ║
║ • No cheating mods                 ║
║ • No unfair advantages             ║
║ • Server compliant                 ║
║                                    ║
║ Made with ❤ for PvP               ║
║                                    ║
╚════════════════════════════════════╝
```

---

## Screen 2: HUD Configuration

```
╔════════════════════════════════════════════════════════════════════════════╗
║ HUD MODULE CONFIGURATION                                                   ║
╠════════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║  ┌──────────────────────────────┐  ┌──────────────────────────────┐      ║
║  │ ☑ FPS Counter               │  │ ☑ Ping Display               │      ║
║  │                              │  │                              │      ║
║  │ Real-time frame rate display │  │ Network latency indicator    │      ║
║  │ with color coding            │  │ with color coding            │      ║
║  │                              │  │                              │      ║
║  │ Scale: ═════════☑═══  1.0x  │  │ Scale: ═════════☑═══  1.0x  │      ║
║  │                              │  │                              │      ║
║  └──────────────────────────────┘  └──────────────────────────────┘      ║
║                                                                            ║
║  ┌──────────────────────────────┐  ┌──────────────────────────────┐      ║
║  │ ☑ Toggle Sprint              │  │ ☑ Combo Counter              │      ║
║  │                              │  │                              │      ║
║  │ Fixes vanilla auto-sprint    │  │ Tracks consecutive hits      │      ║
║  │ inconsistency                │  │ during combat                │      ║
║  │                              │  │                              │      ║
║  │ Scale: ═════════☑═══  1.0x  │  │ Scale: ═════════☑═══  1.0x  │      ║
║  │                              │  │                              │      ║
║  └──────────────────────────────┘  └──────────────────────────────┘      ║
║                                                                            ║
║  ┌────────────────────────────────────────────────────────────────────┐  ║
║  │            SAVE HUD CONFIGURATION                                  │  ║
║  └────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
╚════════════════════════════════════════════════════════════════════════════╝
```

---

## Screen 3: Settings

```
╔════════════════════════════════════════════════════════════════════════════╗
║ SETTINGS                                                                   ║
╠════════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║  ┌────────────────────────────────────────────────────────────────────┐  ║
║  │                                                                    │  ║
║  │ Java Memory:                                                       │  ║
║  │ [4G                                                            ▼] │  ║
║  │                                                                    │  ║
║  │ Theme:                                                             │  ║
║  │ [Dark (Default)                                                ▼] │  ║
║  │                                                                    │  ║
║  │ ☑ Auto-launch on startup                                          │  ║
║  │                                                                    │  ║
║  │ ☑ Keep launcher open after launch                                 │  ║
║  │                                                                    │  ║
║  └────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
║  ┌────────────────────────────────────────────────────────────────────┐  ║
║  │               SAVE SETTINGS                                        │  ║
║  └────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
╚════════════════════════════════════════════════════════════════════════════╝
```

---

## Screen 4: Profiles

```
╔════════════════════════════════════════════════════════════════════════════╗
║ PROFILE MANAGER                                                            ║
╠════════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║  Profile Name:                                                             ║
║  ┌────────────────────────────────────────────────┐  ┌────────────────┐  ║
║  │ MyPvPProfile                                   │  │    CREATE      │  ║
║  └────────────────────────────────────────────────┘  └────────────────┘  ║
║                                                                            ║
║  SAVED PROFILES                                                            ║
║  ┌────────────────────────────────────────────────────────────────────┐  ║
║  │ > Default                                                          │  ║
║  │ > PvP                                                              │  ║
║  │ > Survival                                                         │  ║
║  │ > Creative                                                         │  ║
║  │                                                                    │  ║
║  └────────────────────────────────────────────────────────────────────┘  ║
║                                                                            ║
║  ┌─────────────────────────┐  ┌──────────────────────────────────────┐  ║
║  │        LOAD             │  │            DELETE                   │  ║
║  └─────────────────────────┘  └──────────────────────────────────────┘  ║
║                                                                            ║
╚════════════════════════════════════════════════════════════════════════════╝
```

---

## Color Scheme

```
Background:      #0a0e27  (Dark blue-black)
Panels:          #1a1f3a  (Darker blue)
Primary Accent:  #00ff00  (Neon green)
Secondary:       #888888  (Gray text)
Success:         #00ff00  (Green buttons)
Warning:         #ffff00  (Yellow text)
Errors:          #ff4444  (Red buttons)
Text:            #ffffff  (White)
```

---

## Key Features

✅ **Modular Screens**: Easy navigation with tab-based design
✅ **Green Theme**: Professional gaming aesthetic with neon green
✅ **Real-time Feedback**: Status messages and progress bars
✅ **Profile Management**: Save/load multiple configurations
✅ **HUD Customization**: Scale and toggle each module individually
✅ **Settings Panel**: Quick access to Java memory and theme options
✅ **Responsive**: Works on different screen sizes

---

## UI Technologies

- **Framework**: JavaFX 21.0.2
- **Layout**: FXML (Functional XML)
- **Styling**: CSS (custom dark theme)
- **Architecture**: MVC (Model-View-Controller)

---

## Future Enhancements

- [ ] Custom theme editor
- [ ] HUD position preview
- [ ] Keybinding customization UI
- [ ] Stats dashboard
- [ ] Server status display
- [ ] Mod update notifications
- [ ] Advanced settings panel

---

**Status**: GUI code complete and ready to run with JavaFX SDK installed! 🚀
