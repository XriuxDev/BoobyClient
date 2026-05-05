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
        
        currentContext.getMatrices().pushMatrix();
        currentContext.getMatrices().translate(x, y);
        currentContext.getMatrices().scale(scale, scale);
        
        currentContext.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, true);
        
        currentContext.getMatrices().popMatrix();
    }

    /**
     * Draw a smooth rounded rectangle
     */
    public void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        if (currentContext == null) return;
        
        // Draw main body
        currentContext.fill((int)(x + radius), (int)y, (int)(x + width - radius), (int)(y + height), color);
        currentContext.fill((int)x, (int)(y + radius), (int)(x + radius), (int)(y + height - radius), color);
        currentContext.fill((int)(x + width - radius), (int)(y + radius), (int)(x + width), (int)(y + height - radius), color);
        
        // Draw corners (simulated with smaller rects for now, ideally use a shader or circles)
        drawCorner(x, y, radius, radius, color, true, true);
        drawCorner(x + width - radius, y, radius, radius, color, false, true);
        drawCorner(x, y + height - radius, radius, radius, color, true, false);
        drawCorner(x + width - radius, y + height - radius, radius, radius, color, false, false);
    }

    /**
     * Draw a glow/shadow effect
     */
    public void drawGlow(float x, float y, float width, float height, float size, int color) {
        if (currentContext == null) return;
        for (int i = 0; i < size; i++) {
            float alpha = (float)(size - i) / size;
            int alphaColor = (color & 0x00FFFFFF) | ((int)(alpha * ((color >> 24) & 0xFF)) << 24);
            drawRoundedRect(x - i, y - i, width + (i * 2), height + (i * 2), 4 + i, alphaColor);
        }
    }

    private void drawCorner(float x, float y, float w, float h, int color, boolean left, boolean top) {
        currentContext.fill((int)x, (int)y, (int)(x + w), (int)(y + h), color);
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
