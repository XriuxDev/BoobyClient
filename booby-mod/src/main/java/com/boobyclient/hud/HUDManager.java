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
    private final HUDRenderer renderer;
    private boolean enabled = true;

    public HUDManager(int screenWidth, int screenHeight) {
        this.renderer = new HUDRenderer(screenWidth, screenHeight);
        initializeModules();
        logger.info("HUD Manager initialized with {} modules", modules.size());
    }

    /**
     * Initialize all HUD modules
     */
    private void initializeModules() {
        registerModule(new FPSCounterModule());
        registerModule(new PingDisplayModule());
        registerModule(new ToggleSprintModule());
        registerModule(new ComboCounterModule());
        registerModule(new ReachDisplayModule());
    }

    /**
     * Register a HUD module
     */
    public void registerModule(HUDModule module) {
        modules.put(module.getModuleId(), module);
        logger.info("Registered HUD module: {}", module.getDisplayName());
    }

    /**
     * Render all enabled HUD modules
     */
    public void render() {
        if (!enabled) return;

        // Render standard HUD modules directly. We don't render menu here anymore!
        for (HUDModule module : modules.values()) {
            if (module.isEnabled()) {
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
