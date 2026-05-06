package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

public class ArmorHUDModule extends HUDModule {
    public ArmorHUDModule() {
        super("armor_hud", "Armor HUD");
        this.x = 10;
        this.y = 170;
        this.width = 140;
        this.height = 56;
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        int glow = HUDRenderer.getColor(168, 85, 247, 100);
        renderer.drawModuleSurface(x - 4, y - 4, width + 8, height + 8, glow);
        renderer.drawText("ARMOR", x, y + 1, HUDRenderer.getColor(148, 163, 184), 0.7f);

        String[] labels = {"Helmet", "Chest", "Legs", "Boots"};
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };
        for (int i = 0; i < 4; i++) {
            ItemStack stack = client.player.getEquippedStack(slots[i]);
            String line;
            int color;
            if (stack.isEmpty()) {
                line = labels[i] + ": none";
                color = HUDRenderer.getColor(239, 68, 68);
            } else if (stack.isDamageable()) {
                int max = stack.getMaxDamage();
                int left = max - stack.getDamage();
                int pct = Math.max(0, Math.min(100, (int) ((left * 100.0f) / max)));
                line = labels[i] + ": " + pct + "%";
                color = pct >= 50 ? HUDRenderer.getColor(34, 197, 94)
                        : pct >= 25 ? HUDRenderer.getColor(234, 179, 8)
                        : HUDRenderer.getColor(239, 68, 68);
            } else {
                line = labels[i] + ": ok";
                color = HUDRenderer.getColor(226, 232, 240);
            }

            renderer.drawText(line, x, y + 13 + (i * 10), color, 0.65f);
        }
    }
}
