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

        // GOATED Style Colors
        int backgroundColor = HUDRenderer.getColor(15, 23, 42, 160); // Deep charcoal glass
        int glowColor = HUDRenderer.getColor(99, 102, 241, 100); // Indigo glow

        int textColor;
        if (currentPing < 50) {
            textColor = HUDRenderer.getColor(34, 197, 94); // Green
        } else if (currentPing < 150) {
            textColor = HUDRenderer.getColor(234, 179, 8); // Yellow
        } else {
            textColor = HUDRenderer.getColor(239, 68, 68); // Red
        }

        // Draw Premium Background
        renderer.drawGlow(x - 4, y - 34, 75, 20, 6, glowColor);
        renderer.drawRoundedRect(x - 4, y - 34, 75, 20, 6, backgroundColor);

        // Draw Text
        renderer.drawText("MS", x, y - 28, HUDRenderer.getColor(148, 163, 184), 0.7f);
        renderer.drawText(String.valueOf(currentPing), x + 24, y - 30, textColor, 1.0f);
    }

    @Override
    public void tick() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null && client.player != null) {
            net.minecraft.client.network.PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) {
                currentPing = entry.getLatency();
            }
        }
    }

    public int getCurrentPing() {
        return currentPing;
    }
}
