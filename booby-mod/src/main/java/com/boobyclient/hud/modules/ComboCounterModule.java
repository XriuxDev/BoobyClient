package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combo Counter HUD Module - Tracks consecutive hits on enemies
 */
public class ComboCounterModule extends HUDModule {
    private static final Logger logger = LoggerFactory.getLogger(ComboCounterModule.class);

    private int comboCount = 0;
    private long lastHitTime = 0;
    private static final long COMBO_RESET_TIME = 3000; // Reset combo after 3 seconds

    public ComboCounterModule() {
        super("combo_counter", "Combo Counter");
        this.x = 10;
        this.y = 50;
        logger.info("Combo Counter module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled || comboCount <= 0) return;

        // Reset if timeout
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTime > COMBO_RESET_TIME) {
            comboCount = 0;
            return;
        }

        // GOATED Style Colors
        int backgroundColor = HUDRenderer.getColor(15, 23, 42, 160); // Deep charcoal glass
        int glowColor = HUDRenderer.getColor(251, 191, 36, 120); // Gold glow

        // Draw Premium Background
        renderer.drawGlow(x - 4, y - 54, 90, 20, 8, glowColor);
        renderer.drawRoundedRect(x - 4, y - 54, 90, 20, 6, backgroundColor);

        // Draw Text
        renderer.drawText("COMBO", x, y - 48, HUDRenderer.getColor(148, 163, 184), 0.7f);
        renderer.drawText(comboCount + " HITS", x + 40, y - 50, HUDRenderer.getColor(251, 191, 36), 1.0f);
    }

    /**
     * Called when player hits an entity
     */
    public void onHit() {
        comboCount++;
        lastHitTime = System.currentTimeMillis();
    }

    /**
     * Reset combo counter
     */
    public void resetCombo() {
        comboCount = 0;
    }

    public int getComboCount() {
        return comboCount;
    }
}
