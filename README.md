# Booby Client - Minecraft 1.21+ PvP Launcher

A lightweight Minecraft client launcher with competitive PvP features and quality-of-life enhancements for Minecraft 1.21+.

## Features

### Competitive PvP Features
- **Toggle Sprint** - Fixes vanilla auto-sprint bug (keeps sprint active when holding forward)
- **FPS Counter** - Real-time frames per second display with color coding
- **Ping Display** - Network latency indicator
- **Combo Counter** - Tracks consecutive hits during combat

### Quality of Life
- Account management (multiple profiles)
- Version selector (1.20.x - 1.21+)
- HUD module customization
- Configuration profiles
- Performance monitoring

## Requirements

- Java 17 or higher
- Maven 3.6+
- Windows 10+, macOS, or Linux

## Building

```bash
cd BoobyClient
mvn clean package
```

This will create a fat JAR in `target/booby-client-1.0.0.jar`

## Running

```bash
java -jar target/booby-client-1.0.0.jar
```

Or run directly from Maven:
```bash
mvn javafx:run
```

## Project Structure

```
booby-client/
├── src/main/java/com/boobyclient/
│   ├── launcher/          # Main launcher UI
│   ├── hud/               # HUD system
│   │   └── modules/       # Individual HUD modules
│   ├── config/            # Configuration management
│   └── util/              # Utilities
├── src/main/resources/
│   ├── fxml/              # JavaFX UI files
│   └── css/               # Styling
└── pom.xml
```

## Configuration

Configurations are stored in `~/.boobyclient/`:
- `config.json` - Global settings
- `profiles/` - Per-profile configurations

## Limitations

This launcher is designed as a wrapper/wrapper for Minecraft. To implement full HUD injection and game integration, you would need to:

1. Use a mod framework (Fabric/Forge) for full Minecraft integration
2. Implement bytecode injection via Mixins
3. Hook into Minecraft's rendering pipeline
4. Use JNI for low-level graphics rendering

The current version provides the launcher infrastructure and HUD module system that can be extended with actual Minecraft mods.

## Legal Notice

This client is designed for **vanilla-legal gameplay only**. All features are compliant with standard multiplayer servers and provide no unfair advantages. Never use cheating mods like radar, X-ray, or aim assists on multiplayer servers.

## Development

To add a new HUD module:

1. Create a class extending `HUDModule`
2. Implement `render()` method
3. Register in `HUDManager.initializeModules()`

Example:
```java
public class MyHUDModule extends HUDModule {
    public MyHUDModule() {
        super("my_module", "My Module");
    }

    @Override
    public void render(HUDRenderer renderer) {
        renderer.drawText("Hello!", x, y, color, scale);
    }
}
```

## License

MIT License - Feel free to modify and distribute
