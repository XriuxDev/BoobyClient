package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import net.minecraft.client.MinecraftClient;

public class CoordinatesHUDModule extends HUDModule {
    public CoordinatesHUDModule() {
        super("coordinates_hud", "Coordinates HUD");
        this.x = 10;
        this.y = 232;
        this.width = 125;
        this.height = 20;
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int xPos = (int) Math.floor(client.player.getX());
        int yPos = (int) Math.floor(client.player.getY());
        int zPos = (int) Math.floor(client.player.getZ());

        int glow = HUDRenderer.getColor(16, 185, 129, 110);
        renderer.drawModuleSurface(x - 4, y - 4, 135, 20, glow);
        renderer.drawText("XYZ", x, y + 2, HUDRenderer.getColor(148, 163, 184), 0.7f);
        renderer.drawText(xPos + ", " + yPos + ", " + zPos, x + 22, y, HUDRenderer.getColor(226, 232, 240), 0.85f);
    }
}
