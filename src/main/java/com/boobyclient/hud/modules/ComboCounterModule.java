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
        if (!enabled) return;

        // Check if combo should be reset
        long currentTime = System.currentTimeMillis();
        if (comboCount > 0 && currentTime - lastHitTime > COMBO_RESET_TIME) {
            comboCount = 0;
        }

        if (comboCount > 0) {
            int color = HUDRenderer.getColor(255, 200, 0); // Gold color
            renderer.drawText("COMBO: " + comboCount, x, y, color, scale);
        }
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
