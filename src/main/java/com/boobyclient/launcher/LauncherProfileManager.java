package com.boobyclient.launcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Manages authentication using the official Minecraft Launcher's profile data
 * Reads from the launcher's profiles.json to get logged-in user information
 */
public class LauncherProfileManager {
    private static final Logger logger = LoggerFactory.getLogger(LauncherProfileManager.class);

    private static final String MINECRAFT_DIR = System.getProperty("user.home") + "/.minecraft";
    private static final String LAUNCHER_PROFILES = MINECRAFT_DIR + "/launcher_profiles.json";

    private String username;
    private String uuid;
    private boolean authenticated = false;
    private Gson gson = new Gson();

    public LauncherProfileManager() {
        detectLauncherProfiles();
    }

    /**
     * Detect if official launcher is installed and read profiles
     */
    private void detectLauncherProfiles() {
        try {
            File profilesFile = new File(LAUNCHER_PROFILES);

            if (!profilesFile.exists()) {
                logger.warn("Official Minecraft launcher profiles not found at {}", LAUNCHER_PROFILES);
                logger.info("Please ensure Minecraft Launcher is installed and you're logged in");
                return;
            }

            logger.info("Found Minecraft launcher profiles at {}", LAUNCHER_PROFILES);

            try (FileReader reader = new FileReader(profilesFile)) {
                JsonObject profiles = gson.fromJson(reader, JsonObject.class);

                // Check if there's profile data
                if (profiles.has("profiles")) {
                    JsonObject profilesObj = profiles.get("profiles").getAsJsonObject();

                    // Find the first valid profile with auth data
                    for (String profileName : profilesObj.keySet()) {
                        JsonObject profile = profilesObj.get(profileName).getAsJsonObject();

                        if (profile.has("name") && !profile.get("name").isJsonNull()) {
                            username = profile.get("name").getAsString();

                            if (profile.has("uuid")) {
                                uuid = profile.get("uuid").getAsString();
                            }

                            authenticated = true;
                            logger.info("Found logged-in user: {}", username);
                            return;
                        }
                    }
                }

                // Try to read from "selectedProfile" if it exists
                if (profiles.has("selectedProfile")) {
                    String selectedProfileName = profiles.get("selectedProfile").getAsString();
                    if (profiles.has("profiles")) {
                        JsonObject profilesObj = profiles.get("profiles").getAsJsonObject();
                        if (profilesObj.has(selectedProfileName)) {
                            JsonObject profile = profilesObj.get(selectedProfileName).getAsJsonObject();
                            if (profile.has("name")) {
                                username = profile.get("name").getAsString();
                                if (profile.has("uuid")) {
                                    uuid = profile.get("uuid").getAsString();
                                }
                                authenticated = true;
                                logger.info("Found selected profile: {}", username);
                                return;
                            }
                        }
                    }
                }

                logger.warn("No valid profile found in launcher_profiles.json");

            } catch (Exception e) {
                logger.error("Failed to read launcher profiles", e);
            }

        } catch (Exception e) {
            logger.error("Error detecting launcher profiles", e);
        }
    }

    /**
     * Check if user is authenticated via official launcher
     */
    public boolean isAuthenticated() {
        return authenticated && username != null && !username.isEmpty();
    }

    /**
     * Get username from launcher
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get UUID from launcher
     */
    public String getUUID() {
        return uuid;
    }

    /**
     * Get access token (not available from profiles.json, but would be in newer launcher data)
     * For now, return a placeholder - the launcher will handle actual auth
     */
    public String getAccessToken() {
        // The official launcher stores tokens, but they're not in the JSON
        // We'll use a placeholder that the game launcher can recognize
        return "official-launcher-token";
    }

    /**
     * Get official launcher installation path
     */
    public static String getOfficialLauncherPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows: Check AppData
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                File launcherDir = new File(appData + "\\Roaming\\.minecraft");
                if (launcherDir.exists()) {
                    return launcherDir.getAbsolutePath();
                }
            }
        } else if (os.contains("mac")) {
            // macOS: Check Library
            File launcherDir = new File(System.getProperty("user.home") + "/Library/Application Support/.minecraft");
            if (launcherDir.exists()) {
                return launcherDir.getAbsolutePath();
            }
        } else {
            // Linux
            File launcherDir = new File(System.getProperty("user.home") + "/.minecraft");
            if (launcherDir.exists()) {
                return launcherDir.getAbsolutePath();
            }
        }

        return null;
    }

    /**
     * Open the official Minecraft Launcher for login
     */
    public static void openOfficialLauncher() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows
                Runtime.getRuntime().exec("cmd /c start launcher:");
            } else if (os.contains("mac")) {
                // macOS
                Runtime.getRuntime().exec("open -a 'Minecraft Launcher'");
            } else {
                // Linux
                Runtime.getRuntime().exec("minecraft-launcher");
            }

            logger.info("Opened official Minecraft Launcher");
        } catch (Exception e) {
            logger.error("Failed to open official launcher", e);
        }
    }

    /**
     * Refresh profile data (re-read the profiles.json)
     */
    public void refresh() {
        username = null;
        uuid = null;
        authenticated = false;
        detectLauncherProfiles();
    }
}
