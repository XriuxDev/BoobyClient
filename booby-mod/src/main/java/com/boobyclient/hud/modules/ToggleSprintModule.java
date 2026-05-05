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

    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint");
        this.x = 10;
        this.y = 80;
        logger.info("Toggle Sprint module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        // GOATED Style Colors
        int backgroundColor = HUDRenderer.getColor(15, 23, 42, 160); // Deep charcoal glass
        int glowColor = sprintActive ? HUDRenderer.getColor(34, 197, 94, 100) : HUDRenderer.getColor(99, 102, 241, 60);

        int textColor = sprintActive ? HUDRenderer.getColor(34, 197, 94) : HUDRenderer.getColor(148, 163, 184);

        // Draw Premium Background
        renderer.drawGlow(x - 4, y - 4, 100, 20, 6, glowColor);
        renderer.drawRoundedRect(x - 4, y - 4, 100, 20, 6, backgroundColor);

        // Draw Text
        renderer.drawText("SPRINT", x, y + 2, HUDRenderer.getColor(148, 163, 184), 0.7f);
        renderer.drawText(sprintActive ? "ACTIVE" : "TOGGLED", x + 45, y, textColor, 1.0f);
    }

    @Override
    public void tick() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null && enabled) {
            // If moving forward and toggle is on, force sprint
            if (client.options.forwardKey.isPressed() && !client.player.horizontalCollision && !client.player.isSneaking()) {
                client.player.setSprinting(true);
                sprintActive = true;
            } else {
                sprintActive = false;
            }
        }
    }

    @Override
    public void onInput(int keyCode, int scanCode, int action) {
        // Handle custom toggle key if needed
    }

    public boolean isSprintActive() {
        return sprintActive;
    }
}
