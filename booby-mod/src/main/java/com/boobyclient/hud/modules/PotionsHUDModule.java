package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.Comparator;
import java.util.List;

public class PotionsHUDModule extends HUDModule {
    public PotionsHUDModule() {
        super("potions_hud", "Potions HUD");
        this.x = 10;
        this.y = 258;
        this.width = 165;
        this.height = 56;
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int glow = HUDRenderer.getColor(236, 72, 153, 105);
        renderer.drawModuleSurface(x - 4, y - 4, width + 8, height + 8, glow);
        renderer.drawText("POTIONS", x, y + 1, HUDRenderer.getColor(148, 163, 184), 0.7f);

        List<StatusEffectInstance> effects = client.player.getStatusEffects().stream()
                .sorted(Comparator.comparingInt(StatusEffectInstance::getDuration).reversed())
                .limit(4)
                .toList();

        if (effects.isEmpty()) {
            renderer.drawText("No active effects", x, y + 14, HUDRenderer.getColor(148, 163, 184), 0.7f);
            return;
        }

        int row = 0;
        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            int seconds = Math.max(0, effect.getDuration() / 20);
            int minutes = seconds / 60;
            int remainder = seconds % 60;
            String timer = String.format("%d:%02d", minutes, remainder);

            int amplifier = effect.getAmplifier() + 1;
            String line = effectName + " " + amplifier + " - " + timer;
            renderer.drawText(line, x, y + 14 + (row * 10), HUDRenderer.getColor(226, 232, 240), 0.65f);
            row++;
        }
    }
}
