package com.boobyclient.hud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles rendering of HUD elements
 */
public class HUDRenderer {
    private static final Logger logger = LoggerFactory.getLogger(HUDRenderer.class);

    private int screenWidth;
    private int screenHeight;

    public HUDRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        logger.info("HUDRenderer initialized for {}x{}", screenWidth, screenHeight);
    }

    /**
     * Draw text at position
     */
    public void drawText(String text, float x, float y, int color, float scale) {
        // In a real implementation, this would use Minecraft's font renderer
        // For now, this is a placeholder
    }

    /**
     * Draw a rectangle
     */
    public void drawRect(float x, float y, float width, float height, int color) {
        // In a real implementation, this would use OpenGL or Minecraft's rendering
        // For now, this is a placeholder
    }

    /**
     * Draw a filled rectangle
     */
    public void drawFilledRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, height, color);
    }

    /**
     * Draw a border
     */
    public void drawBorder(float x, float y, float width, float height, int color, float thickness) {
        // Draw border lines
    }

    /**
     * Update screen dimensions (called on window resize)
     */
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        logger.info("Screen size updated to {}x{}", width, height);
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    // Color utilities
    public static int getColor(int r, int g, int b) {
        return getColor(r, g, b, 255);
    }

    public static int getColor(int r, int g, int b, int a) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
