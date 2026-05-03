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

        // Color: green if >60 FPS, yellow if 30-60, red if <30
        int color;
        if (currentFPS >= 60) {
            color = HUDRenderer.getColor(0, 255, 0); // Green
        } else if (currentFPS >= 30) {
            color = HUDRenderer.getColor(255, 255, 0); // Yellow
        } else {
            color = HUDRenderer.getColor(255, 0, 0); // Red
        }

        renderer.drawText("FPS: " + currentFPS, x, y, color, scale);
    }

    public int getCurrentFPS() {
        return currentFPS;
    }
}
