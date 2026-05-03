package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ping Display HUD Module - Shows current network latency
 */
public class PingDisplayModule extends HUDModule {
    private static final Logger logger = LoggerFactory.getLogger(PingDisplayModule.class);

    private int currentPing = 0;
    private long lastPingUpdate = System.currentTimeMillis();

    public PingDisplayModule() {
        super("ping_display", "Ping Display");
        this.x = 10;
        this.y = 30;
        logger.info("Ping Display module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        // Simulate ping update (in real implementation, would query server)
        long now = System.currentTimeMillis();
        if (now - lastPingUpdate > 500) {
            updatePing();
            lastPingUpdate = now;
        }

        // Color: green if <50ms, yellow if 50-150ms, red if >150ms
        int color;
        if (currentPing < 50) {
            color = HUDRenderer.getColor(0, 255, 0); // Green
        } else if (currentPing < 150) {
            color = HUDRenderer.getColor(255, 255, 0); // Yellow
        } else {
            color = HUDRenderer.getColor(255, 0, 0); // Red
        }

        renderer.drawText("PING: " + currentPing + "ms", x, y, color, scale);
    }

    private void updatePing() {
        // In real implementation, this would measure actual network latency
        // For now, simulate with a random value
        currentPing = 20 + (int)(Math.random() * 50);
    }

    public int getCurrentPing() {
        return currentPing;
    }
}
