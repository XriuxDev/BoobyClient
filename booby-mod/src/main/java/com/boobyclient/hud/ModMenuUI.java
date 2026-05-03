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
    
    /**
     * Render the mod menu overlay
     */
    public void render() {
        if (!isVisible) return;
        
        HUDRenderer renderer = hudManager.getRenderer();
        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        
        // 1. Draw semi-transparent dark overlay over the whole screen
        // In a real mixin, this would render using OpenGL blending
        renderer.drawFilledRect(0, 0, screenW, screenH, HUDRenderer.getColor(0, 0, 0, 180));
        
        // 2. Draw Menu Background Panel (Centered)
        float panelWidth = 300;
        float panelHeight = 400;
        float startX = (screenW - panelWidth) / 2f;
        float startY = (screenH - panelHeight) / 2f;
        
        // Zinc-900 background with indigo border
        renderer.drawFilledRect(startX, startY, panelWidth, panelHeight, HUDRenderer.getColor(24, 24, 27, 255));
        renderer.drawBorder(startX, startY, panelWidth, panelHeight, HUDRenderer.getColor(99, 102, 241, 255), 2.0f);
        
        // 3. Draw Menu Title
        renderer.drawText("BOOBY CLIENT", startX + panelWidth/2f - 40, startY + 20, HUDRenderer.getColor(255, 255, 255), 1.5f);
        renderer.drawText("Mod Menu", startX + panelWidth/2f - 25, startY + 40, HUDRenderer.getColor(161, 161, 170), 1.0f);
        
        // 4. Render toggle buttons for each HUD module
        float yOffset = 0;
        for (HUDModule module : hudManager.getAllModules()) {
            float btnX = startX + 20;
            float btnY = startY + 70 + yOffset;
            float btnWidth = panelWidth - 40;
            float btnHeight = 40;
            
            // Button Background (Green if enabled, Red if disabled)
            int bgColor = module.isEnabled() ? HUDRenderer.getColor(16, 185, 129, 200) : HUDRenderer.getColor(239, 68, 68, 200);
            renderer.drawFilledRect(btnX, btnY, btnWidth, btnHeight, bgColor);
            
            // Text
            String status = module.isEnabled() ? "ON" : "OFF";
            renderer.drawText(module.getDisplayName() + ": " + status, btnX + 10, btnY + 12, HUDRenderer.getColor(255, 255, 255), 1.2f);
            
            yOffset += 50;
        }
        
        // 5. Draw Footer
        renderer.drawText("Press Right Shift to close", startX + panelWidth/2f - 60, startY + panelHeight - 20, HUDRenderer.getColor(161, 161, 170), 0.9f);
    }
    
    public boolean isVisible() {
        return isVisible;
    }
}
