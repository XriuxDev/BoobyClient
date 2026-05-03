package com.boobyclient.launcher;

import com.boobyclient.config.ConfigManager;
import com.boobyclient.hud.HUDManager;
import com.boobyclient.util.GameLauncher;

import java.util.Scanner;

/**
 * CLI Launcher for Booby Client - works without JavaFX dependencies
 */
public class CLILauncher {

    private ConfigManager configManager;
    private GameLauncher gameLauncher;
    private HUDManager hudManager;

    public CLILauncher() {
        this.configManager = new ConfigManager();
        this.gameLauncher = new GameLauncher();
        this.hudManager = new HUDManager(1920, 1080); // Default screen size
    }

    public void start() {
        printBanner();

        while (true) {
            printMenu();
            String choice = getUserInput("Enter your choice: ");

            switch (choice.toLowerCase()) {
                case "1":
                    launchGame();
                    break;
                case "2":
                    manageProfiles();
                    break;
                case "3":
                    configureHUD();
                    break;
                case "4":
                    showInfo();
                    break;
                case "5":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void printBanner() {
        System.out.println("\n");
        System.out.println("╔═══════════════════════════════════════╗");
        System.out.println("║     BOOBY CLIENT - Minecraft 1.21+    ║");
        System.out.println("║       PvP Launcher & HUD Manager      ║");
        System.out.println("╚═══════════════════════════════════════╝");
        System.out.println();
    }

    private void printMenu() {
        System.out.println("\n========== Main Menu ==========");
        System.out.println("1. Launch Game");
        System.out.println("2. Manage Profiles");
        System.out.println("3. Configure HUD");
        System.out.println("4. Show Client Info");
        System.out.println("5. Exit");
        System.out.println("==============================\n");
    }

    private void launchGame() {
        System.out.println("\n[*] Preparing to launch Minecraft...");

        String version = getUserInput("Minecraft Version (1.21, 1.20.4): ");
        if (version.isEmpty()) version = "1.21";

        String account = getUserInput("Account Name: ");
        if (account.isEmpty()) account = "Player";

        System.out.println("\n[*] HUD Modules Configuration:");
        boolean toggleSprint = getYesNo("  - Enable Toggle Sprint? (Y/n): ");
        boolean fpsCounter = getYesNo("  - Enable FPS Counter? (Y/n): ");
        boolean pingDisplay = getYesNo("  - Enable Ping Display? (Y/n): ");
        boolean comboCounter = getYesNo("  - Enable Combo Counter? (Y/n): ");

        GameLauncher.LaunchConfig config = new GameLauncher.LaunchConfig();
        config.version = version;
        config.account = account;
        config.hudModules = new GameLauncher.HUDConfig(toggleSprint, fpsCounter, pingDisplay, comboCounter);

        System.out.println("\n[*] Launch Configuration:");
        System.out.println("    Version: " + config.version);
        System.out.println("    Account: " + config.account);
        System.out.println("    HUD Modules: Sprint=" + toggleSprint + ", FPS=" + fpsCounter + ", Ping=" + pingDisplay + ", Combo=" + comboCounter);

        try {
            System.out.println("\n[*] Starting game...");
            gameLauncher.launch(config);
            System.out.println("[✓] Game launched successfully!");
        } catch (Exception e) {
            System.err.println("[✗] Failed to launch game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void manageProfiles() {
        System.out.println("\n========== Profile Manager ==========");
        System.out.println("1. Load Profile");
        System.out.println("2. Save Current Profile");
        System.out.println("3. List Profiles");
        System.out.println("4. Back to Main Menu");

        String choice = getUserInput("Enter your choice: ");

        switch (choice) {
            case "1":
                loadProfile();
                break;
            case "2":
                saveProfile();
                break;
            case "3":
                listProfiles();
                break;
            case "4":
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void loadProfile() {
        listProfiles();
        String profileName = getUserInput("Enter profile name to load: ");
        var profile = configManager.loadProfile(profileName);
        System.out.println("[✓] Loaded profile: " + profileName);
        System.out.println("    Settings: " + profile);
    }

    private void saveProfile() {
        String profileName = getUserInput("Enter profile name to save: ");
        configManager.saveProfile(profileName, configManager.getAvailableProfiles().isEmpty() ?
            java.util.Map.of() : java.util.Map.of());
        System.out.println("[✓] Saved profile: " + profileName);
    }

    private void listProfiles() {
        var profiles = configManager.getAvailableProfiles();
        System.out.println("[*] Available Profiles:");
        if (profiles.isEmpty()) {
            System.out.println("    (No profiles found)");
        } else {
            for (String profile : profiles) {
                System.out.println("    - " + profile);
            }
        }
    }

    private void configureHUD() {
        System.out.println("\n========== HUD Configuration ==========");
        System.out.println("Available HUD Modules:");

        for (var module : hudManager.getAllModules()) {
            String status = module.isEnabled() ? "[✓]" : "[ ]";
            System.out.println("  " + status + " " + module.getDisplayName());
        }

        String moduleId = getUserInput("\nEnter module ID to toggle (or 'back' to return): ");
        if (!moduleId.equalsIgnoreCase("back")) {
            var module = hudManager.getModule(moduleId);
            if (module != null) {
                boolean newState = !module.isEnabled();
                hudManager.toggleModule(moduleId, newState);
                System.out.println("[✓] " + module.getDisplayName() + " is now " + (newState ? "ENABLED" : "DISABLED"));
            } else {
                System.out.println("[✗] Module not found: " + moduleId);
            }
        }
    }

    private void showInfo() {
        System.out.println("\n========== Client Information ==========");
        System.out.println("Version: 1.0.0");
        System.out.println("Build: CLI Edition");
        System.out.println("Target: Minecraft 1.21+");
        System.out.println("Type: PvP Client Launcher");
        System.out.println("\nFeatures:");
        System.out.println("  ✓ Toggle Sprint (fixes vanilla auto-sprint)");
        System.out.println("  ✓ FPS Counter with color coding");
        System.out.println("  ✓ Ping Display (real-time latency)");
        System.out.println("  ✓ Combo Counter (hit tracking)");
        System.out.println("  ✓ Profile Management");
        System.out.println("  ✓ HUD Module System");
        System.out.println("\nLegal Notice: This client contains ONLY vanilla-legal features.");
        System.out.println("No cheating mods (radar, X-ray, aim assist) included.");
        System.out.println("=======================================\n");
    }

    private String getUserInput(String prompt) {
        System.out.print(prompt);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim();
    }

    private boolean getYesNo(String prompt) {
        String input = getUserInput(prompt).toLowerCase();
        return input.isEmpty() || input.startsWith("y");
    }

    public static void main(String[] args) {
        try {
            CLILauncher launcher = new CLILauncher();
            launcher.start();
        } catch (Exception e) {
            System.err.println("Fatal error starting launcher:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
