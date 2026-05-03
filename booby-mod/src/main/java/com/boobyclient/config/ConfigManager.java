package com.boobyclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Manages configuration files and settings
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.boobyclient";
    private static final String PROFILES_DIR = CONFIG_DIR + "/profiles";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, Object> config;
    private String currentProfile = "default";

    static {
        // Create directories if they don't exist
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
            logger.info("Created config directory at {}", CONFIG_DIR);
        }

        File profilesDir = new File(PROFILES_DIR);
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
            logger.info("Created profiles directory at {}", PROFILES_DIR);
        }
    }

    public ConfigManager() {
        config = new HashMap<>();
        loadGlobalConfig();
    }

    /**
     * Load global configuration
     */
    private void loadGlobalConfig() {
        File configFile = new File(CONFIG_DIR + "/config.json");
        if (configFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(configFile.toPath()));
                config = GSON.fromJson(content, Map.class);
                logger.info("Loaded global configuration");
            } catch (Exception e) {
                logger.error("Failed to load global config", e);
                config = getDefaultConfig();
            }
        } else {
            config = getDefaultConfig();
            saveGlobalConfig();
        }
    }

    /**
     * Save global configuration
     */
    public void saveGlobalConfig() {
        File configFile = new File(CONFIG_DIR + "/config.json");
        try {
            String json = GSON.toJson(config);
            Files.write(configFile.toPath(), json.getBytes());
            logger.info("Saved global configuration");
        } catch (Exception e) {
            logger.error("Failed to save global config", e);
        }
    }

    /**
     * Load profile configuration
     */
    public Map<String, Object> loadProfile(String profileName) {
        File profileFile = new File(PROFILES_DIR + "/" + profileName + ".json");
        Map<String, Object> profileConfig = new HashMap<>();

        if (profileFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(profileFile.toPath()));
                profileConfig = GSON.fromJson(content, Map.class);
                logger.info("Loaded profile: {}", profileName);
            } catch (Exception e) {
                logger.error("Failed to load profile: {}", profileName, e);
            }
        } else {
            logger.warn("Profile not found: {}", profileName);
        }

        currentProfile = profileName;
        return profileConfig;
    }

    /**
     * Save profile configuration
     */
    public void saveProfile(String profileName, Map<String, Object> profileConfig) {
        File profileFile = new File(PROFILES_DIR + "/" + profileName + ".json");
        try {
            String json = GSON.toJson(profileConfig);
            Files.write(profileFile.toPath(), json.getBytes());
            logger.info("Saved profile: {}", profileName);
        } catch (Exception e) {
            logger.error("Failed to save profile: {}", profileName, e);
        }
    }

    /**
     * Get default configuration
     */
    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("theme", "dark");
        defaults.put("language", "en");
        defaults.put("defaultProfile", "default");
        defaults.put("autoLaunch", false);
        defaults.put("javaMemory", "4G");
        defaults.put("hudScale", 1.0);
        return defaults;
    }

    /**
     * Get config value
     */
    public Object getConfig(String key) {
        return config.get(key);
    }

    /**
     * Set config value
     */
    public void setConfig(String key, Object value) {
        config.put(key, value);
    }

    /**
     * Get list of available profiles
     */
    public List<String> getAvailableProfiles() {
        List<String> profiles = new ArrayList<>();
        File profilesDir = new File(PROFILES_DIR);
        if (profilesDir.exists()) {
            File[] files = profilesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    profiles.add(file.getName().replace(".json", ""));
                }
            }
        }
        return profiles;
    }

    public String getCurrentProfile() {
        return currentProfile;
    }

    public String getConfigDir() {
        return CONFIG_DIR;
    }
}
