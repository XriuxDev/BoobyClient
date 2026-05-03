package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Toggle Sprint HUD Module - Fixes vanilla auto-sprint inconsistency
 * Keeps sprint state active when holding forward
 */
public class ToggleSprintModule extends HUDModule {
    private static final Logger logger = LoggerFactory.getLogger(ToggleSprintModule.class);

    private boolean sprintActive = false;
    private boolean wasSprintingLastTick = false;

    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint");
        this.x = 10;
        this.y = 100;
        logger.info("Toggle Sprint module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        int color = sprintActive ? HUDRenderer.getColor(0, 255, 0) : HUDRenderer.getColor(100, 100, 100);
        renderer.drawText("SPRINT: " + (sprintActive ? "ON" : "OFF"), x, y, color, scale);
    }

    @Override
    public void tick() {
        // This would integrate with Minecraft's input handling
        // For now, simulate sprint state
        if (wasSprintingLastTick) {
            sprintActive = true;
        }
    }

    @Override
    public void onInput(int keyCode, int scanCode, int action) {
        // keyCode 48 = 'W' key
        // action: 1 = pressed, 0 = released
        if (keyCode == 48) {
            if (action == 1) {
                sprintActive = true;
                wasSprintingLastTick = true;
            } else if (action == 0) {
                sprintActive = false;
                wasSprintingLastTick = false;
            }
        }
    }

    public boolean isSprintActive() {
        return sprintActive;
    }
}
