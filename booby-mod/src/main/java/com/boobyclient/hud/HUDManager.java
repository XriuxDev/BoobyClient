package com.boobyclient.hud;

import com.boobyclient.hud.modules.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages all HUD modules and coordinates rendering
 */
public class HUDManager {
    private static final Logger logger = LoggerFactory.getLogger(HUDManager.class);

    private final Map<String, HUDModule> modules = new LinkedHashMap<>();
    private final Map<String, HUDModule> invisibleModules = new LinkedHashMap<>();
    private final HUDRenderer renderer;
    private boolean enabled = true;

    public HUDManager(int screenWidth, int screenHeight) {
        this.renderer = new HUDRenderer(screenWidth, screenHeight);
        initializeModules();
        loadConfig();
        logger.info("HUD Manager initialized with {} modules", modules.size());
    }

    /**
     * Load configuration from current profile
     */
    public void loadConfig() {
        try {
            com.boobyclient.config.ConfigManager configManager = new com.boobyclient.config.ConfigManager();
            String profile = (String) configManager.getConfig("currentProfile");
            if (profile == null) profile = "Default";
            
            Map<String, Object> profileData = configManager.loadProfile(profile);
            if (profileData != null && profileData.containsKey("showModuleBackgrounds")) {
                HUDRenderer.setModuleBackgroundsEnabled((Boolean) profileData.get("showModuleBackgrounds"));
            }
            if (profileData != null && profileData.containsKey("modules")) {
                Map<String, Map<String, Object>> modulesData = (Map<String, Map<String, Object>>) profileData.get("modules");
                for (Map.Entry<String, Map<String, Object>> entry : modulesData.entrySet()) {
                    HUDModule module = modules.get(entry.getKey());
                    if (module != null) {
                        Map<String, Object> data = entry.getValue();
                        if (data.containsKey("x")) module.setX(((Double) data.get("x")).floatValue());
                        if (data.containsKey("y")) module.setY(((Double) data.get("y")).floatValue());
                        if (data.containsKey("enabled")) module.setEnabled((Boolean) data.get("enabled"));
                    }
                }
                logger.info("Loaded HUD config for profile: {}", profile);
            }
        } catch (Exception e) {
            logger.error("Failed to load HUD config", e);
        }
    }

    /**
     * Save configuration to current profile
     */
    public void saveConfig() {
        try {
            com.boobyclient.config.ConfigManager configManager = new com.boobyclient.config.ConfigManager();
            String profile = (String) configManager.getConfig("currentProfile");
            if (profile == null) profile = "Default";

            Map<String, Object> profileData = configManager.loadProfile(profile);
            Map<String, Map<String, Object>> modulesData = new HashMap<>();

            for (HUDModule module : modules.values()) {
                Map<String, Object> data = new HashMap<>();
                data.put("x", (double) module.getX());
                data.put("y", (double) module.getY());
                data.put("enabled", module.isEnabled());
                modulesData.put(module.getModuleId(), data);
            }

            profileData.put("modules", modulesData);
            profileData.put("showModuleBackgrounds", HUDRenderer.isModuleBackgroundsEnabled());
            configManager.saveProfile(profile, profileData);
            logger.info("Saved HUD config for profile: {}", profile);
        } catch (Exception e) {
            logger.error("Failed to save HUD config", e);
        }
    }

    /**
     * Initialize all HUD modules
     */
    private void initializeModules() {
        registerModule(new FPSCounterModule());
        registerModule(new PingDisplayModule());
        registerModule(new CPSCounterModule());
        registerModule(new CoordinatesHUDModule());
        registerModule(new ArmorHUDModule());
        registerModule(new PotionsHUDModule());
        registerModule(new ToggleSprintModule());
        registerModule(new ComboCounterModule());
        registerModule(new ReachDisplayModule());
        registerInvisibleModule(new ZoomModule());
        registerInvisibleModule(new FullBrightModule());
    }

    /**
     * Register a HUD module that renders on screen
     */
    public void registerModule(HUDModule module) {
        modules.put(module.getModuleId(), module);
        logger.info("Registered HUD module: {}", module.getDisplayName());
    }

    /**
     * Register an invisible module (no on-screen box, only in menu)
     */
    public void registerInvisibleModule(HUDModule module) {
        invisibleModules.put(module.getModuleId(), module);
        modules.put(module.getModuleId(), module); // Also add to modules for menu
        logger.info("Registered invisible module: {}", module.getDisplayName());
    }

    /**
     * Render all enabled HUD modules
     */
    public void render() {
        if (!enabled) return;

        // Render standard HUD modules directly. We don't render menu here anymore!
        for (HUDModule module : modules.values()) {
            if (module.isEnabled() && !invisibleModules.containsKey(module.getModuleId())) {
                try {
                    module.render(renderer);
                } catch (Exception e) {
                    logger.error("Error rendering module: {}", module.getModuleId(), e);
                }
            }
        }
    }

    /**
     * Update all modules (tick)
     */
    public void tick() {
        if (!enabled) return;

        for (HUDModule module : modules.values()) {
            if (module.isEnabled()) {
                try {
                    module.tick();
                } catch (Exception e) {
                    logger.error("Error ticking module: {}", module.getModuleId(), e);
                }
            }
        }
    }

    /**
     * Get a specific module by ID
     */
    public HUDModule getModule(String moduleId) {
        return modules.get(moduleId);
    }

    /**
     * Get all modules
     */
    public Collection<HUDModule> getAllModules() {
        return modules.values();
    }

    /**
     * Toggle module enabled state
     */
    public void toggleModule(String moduleId, boolean enabled) {
        HUDModule module = modules.get(moduleId);
        if (module != null) {
            module.setEnabled(enabled);
            logger.info("Module {} toggled to: {}", moduleId, enabled);
        }
    }

    /**
     * Update screen size
     */
    public void updateScreenSize(int width, int height) {
        renderer.updateScreenSize(width, height);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HUDRenderer getRenderer() {
        return renderer;
    }
}
