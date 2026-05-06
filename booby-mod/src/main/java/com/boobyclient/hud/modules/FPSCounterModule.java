package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FPS Counter HUD Module - Displays current frames per second
 */
public class FPSCounterModule extends HUDModule {
    private static final Logger logger = LoggerFactory.getLogger(FPSCounterModule.class);

    private int currentFPS = 0;
    private long lastUpdateTime = System.currentTimeMillis();
    private int frameCount = 0;
    private static final long UPDATE_INTERVAL = 1000; // Update every second

    public FPSCounterModule() {
        super("fps_counter", "FPS Counter");
        this.x = 10;
        this.y = 10;
        logger.info("FPS Counter module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        frameCount++;
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - lastUpdateTime;

        if (elapsed >= UPDATE_INTERVAL) {
            currentFPS = frameCount;
            frameCount = 0;
            lastUpdateTime = currentTime;
        }

        // GOATED Style Colors
        int glowColor = HUDRenderer.getColor(99, 102, 241, 100); // Indigo glow

        int textColor;
        if (currentFPS >= 60) {
            textColor = HUDRenderer.getColor(34, 197, 94); // Green
        } else if (currentFPS >= 30) {
            textColor = HUDRenderer.getColor(234, 179, 8); // Yellow
        } else {
            textColor = HUDRenderer.getColor(239, 68, 68); // Red
        }

        // Draw Premium Background
        renderer.drawModuleSurface(x - 4, y - 4, 60, 20, glowColor);

        // Draw Text
        renderer.drawText("FPS", x, y + 2, HUDRenderer.getColor(148, 163, 184), 0.7f); // Muted label
        renderer.drawText(String.valueOf(currentFPS), x + 24, y, textColor, 1.0f); // Bright value
    }

    public int getCurrentFPS() {
        return currentFPS;
    }
}
