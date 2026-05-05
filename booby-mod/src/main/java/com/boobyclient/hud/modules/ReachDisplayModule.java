package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reach Display HUD Module - Shows hit distance in blocks
 */
public class ReachDisplayModule extends HUDModule {
    private static final Logger logger = LoggerFactory.getLogger(ReachDisplayModule.class);

    private float lastReach = 0.0f;
    private long lastHitTime = 0;

    public ReachDisplayModule() {
        super("reach_display", "Reach Display");
        this.x = 10;
        this.y = 120;
        this.width = 80;
        this.height = 20;
        logger.info("Reach Display module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        // GOATED Style Colors
        int backgroundColor = HUDRenderer.getColor(15, 23, 42, 160); // Deep charcoal glass
        int glowColor = HUDRenderer.getColor(249, 115, 22, 100); // Orange glow for combat
        
        // Only show if it's been less than 2 seconds since last hit
        if (System.currentTimeMillis() - lastHitTime > 2000) {
            lastReach = 0.0f;
        }

        // Draw Premium Background
        renderer.drawGlow(x - 4, y - 4, 90, 20, 6, glowColor);
        renderer.drawRoundedRect(x - 4, y - 4, 90, 20, 6, backgroundColor);

        // Draw Text
        renderer.drawText("REACH", x, y + 2, HUDRenderer.getColor(148, 163, 184), 0.7f);
        String reachText = String.format("%.2f blocks", lastReach);
        renderer.drawText(reachText, x + 40, y, HUDRenderer.getColor(255, 255, 255), 0.8f);
    }

    public void setLastReach(float reach) {
        this.lastReach = reach;
        this.lastHitTime = System.currentTimeMillis();
    }

    @Override
    public void tick() {
    }
}
