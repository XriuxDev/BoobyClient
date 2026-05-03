package com.boobyclient.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles rendering of HUD elements
 */
public class HUDRenderer {
    private static final Logger logger = LoggerFactory.getLogger(HUDRenderer.class);

    private int screenWidth;
    private int screenHeight;
    private DrawContext currentContext;

    public HUDRenderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        logger.info("HUDRenderer initialized for {}x{}", screenWidth, screenHeight);
    }

    public void setContext(DrawContext context) {
        this.currentContext = context;
    }

    /**
     * Draw text at position
     */
    public void drawText(String text, float x, float y, int color, float scale) {
        if (currentContext == null) return;
        
        currentContext.getMatrices().push();
        currentContext.getMatrices().translate(x, y, 0);
        currentContext.getMatrices().scale(scale, scale, 1.0f);
        
        currentContext.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, true);
        
        currentContext.getMatrices().pop();
    }

    /**
     * Draw a rectangle
     */
    public void drawRect(float x, float y, float width, float height, int color) {
        if (currentContext == null) return;
        currentContext.fill((int)x, (int)y, (int)(x + width), (int)(y + height), color);
    }

    /**
     * Draw a filled rectangle
     */
    public void drawFilledRect(float x, float y, float width, float height, int color) {
        drawRect(x, y, width, height, color);
    }

    public void drawBorder(float x, float y, float width, float height, int color, float thickness) {
        if (currentContext == null) return;
        // Top
        currentContext.fill((int)x, (int)y, (int)(x + width), (int)(y + thickness), color);
        // Bottom
        currentContext.fill((int)x, (int)(y + height - thickness), (int)(x + width), (int)(y + height), color);
        // Left
        currentContext.fill((int)x, (int)y, (int)(x + thickness), (int)(y + height), color);
        // Right
        currentContext.fill((int)(x + width - thickness), (int)y, (int)(x + width), (int)(y + height), color);
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
