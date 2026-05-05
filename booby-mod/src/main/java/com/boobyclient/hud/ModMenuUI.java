package com.boobyclient.hud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In-Game Mod Menu Overlay
 * Toggled via Right Shift.
 */
public class ModMenuUI {
    private static final Logger logger = LoggerFactory.getLogger(ModMenuUI.class);
    
    // GLFW KeyCode for Right Shift is 344
    public static final int KEY_RIGHT_SHIFT = 344;
    
    private final HUDManager hudManager;
    private boolean isVisible = false;
    
    public ModMenuUI(HUDManager hudManager) {
        this.hudManager = hudManager;
        logger.info("ModMenuUI initialized");
    }
    
    /**
     * Handle keyboard input to toggle menu
     */
    public boolean handleInput(int keyCode, int action) {
        // Action 1 is KEY_PRESS
        if (keyCode == KEY_RIGHT_SHIFT && action == 1) {
            isVisible = !isVisible;
            logger.info("Mod Menu toggled: {}", isVisible);
            return true; // Input handled
        }
        
        // If menu is visible, block other inputs from reaching the game
        return isVisible;
    }
    
    /**
     * Render the Mod Menu overlay
     */
    public void render(HUDRenderer renderer) {
        if (!isVisible) return;
        
        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        
        // --- GOATED Menu Design ---
        float menuW = 320;
        float menuH = 240;
        float x = (screenW / 2f) - (menuW / 2f);
        float y = (screenH / 2f) - (menuH / 2f);
        
        // 1. Shadow/Glow
        renderer.drawGlow(x, y, menuW, menuH, 15, HUDRenderer.getColor(99, 102, 241, 80));
        
        // 2. Main Glass Panel
        renderer.drawRoundedRect(x, y, menuW, menuH, 12, HUDRenderer.getColor(15, 23, 42, 230));
        
        // 3. Title Bar
        renderer.drawRoundedRect(x, y, menuW, 35, 12, HUDRenderer.getColor(99, 102, 241, 180));
        renderer.drawText("BOOBY CLIENT", x + 15, y + 10, HUDRenderer.getColor(255, 255, 255), 1.0f);
        renderer.drawText("MOD SETTINGS", x + 15, y + 22, HUDRenderer.getColor(200, 200, 200, 150), 0.6f);
        
        // 4. Module Toggles
        float yOffset = 50;
        for (HUDModule module : hudManager.getAllModules()) {
            boolean enabled = module.isEnabled();
            int statusColor = enabled ? HUDRenderer.getColor(34, 197, 94) : HUDRenderer.getColor(239, 68, 68);
            
            // Module Card
            renderer.drawRoundedRect(x + 10, y + yOffset, menuW - 20, 35, 8, HUDRenderer.getColor(30, 41, 59, 180));
            
            // Text Info
            renderer.drawText(module.getDisplayName(), x + 25, y + yOffset + 12, HUDRenderer.getColor(226, 232, 240), 0.9f);
            
            // Toggle Switch Simulation
            renderer.drawRoundedRect(x + menuW - 60, y + yOffset + 10, 35, 15, 7, HUDRenderer.getColor(15, 23, 42));
            renderer.drawRoundedRect(x + menuW - (enabled ? 40 : 58), y + yOffset + 12, 11, 11, 5, statusColor);
            
            yOffset += 42;
        }
    }
    
    /**
     * Handle mouse clicks for toggling modules
     */
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (!isVisible) return false;
        
        // This is a placeholder for actual click detection geometry.
        // In the real implementation, we would calculate bounding boxes 
        // for each module button and check if (mouseX, mouseY) falls within.
        
        HUDRenderer renderer = hudManager.getRenderer();
        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        
        float startX = (screenW / 2f) - 150;
        float startY = (screenH / 2f) - 100;
        float yOffset = 0;
        
        for (HUDModule module : hudManager.getAllModules()) {
            // Check if click is within this module's button box
            float boxX = startX + 20;
            float boxY = startY + 50 + yOffset;
            float boxW = 260;
            float boxH = 40;
            
            if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH) {
                // Toggled!
                module.setEnabled(!module.isEnabled());
                logger.info("Toggled {} to {}", module.getDisplayName(), module.isEnabled());
                return true;
            }
            
            yOffset += 50;
        }
        
        return true; // Block clicks to the game while menu is open
    }
    
    public boolean isVisible() {
        return isVisible;
    }
}
