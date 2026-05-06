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

    public PingDisplayModule() {
        super("ping_display", "Ping Display");
        this.x = 10;
        this.y = 40;
        this.width = 65;
        this.height = 20;
        logger.info("Ping Display module initialized");
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        // GOATED Style Colors
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
        renderer.drawModuleSurface(x - 4, y - 4, 75, 20, glowColor);

        // Draw Text
        renderer.drawText("MS", x, y + 2, HUDRenderer.getColor(148, 163, 184), 0.7f);
        renderer.drawText(String.valueOf(currentPing), x + 24, y, textColor, 1.0f);
    }

    @Override
    public void tick() {
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (!enabled) return;
        if (client.player == null || client.getNetworkHandler() == null) {
            currentPing = 0;
            return;
        }

        if (client.isInSingleplayer()) {
            currentPing = 0;
            return;
        }

        net.minecraft.client.network.PlayerListEntry ownEntry =
                client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        if (ownEntry != null) {
            int ping = ownEntry.getLatency();
            if (ping >= 0) {
                currentPing = ping;
                return;
            }
        }

        currentPing = 0;
    }

    public int getCurrentPing() {
        return currentPing;
    }
}
