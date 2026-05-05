package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import com.boobyclient.hud.modules.ComboCounterModule;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.network.ClientPlayerInteractionManager.class)
public class CombatMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(net.minecraft.entity.player.PlayerEntity player, net.minecraft.entity.Entity target, CallbackInfo ci) {
        if (BoobyMod.hudManager != null) {
            ComboCounterModule combo = (ComboCounterModule) BoobyMod.hudManager.getModule("combo_counter");
            if (combo != null) {
                combo.onHit();
            }
            
            com.boobyclient.hud.modules.ReachDisplayModule reach = (com.boobyclient.hud.modules.ReachDisplayModule) BoobyMod.hudManager.getModule("reach_display");
            if (reach != null) {
                // Subtract hitbox offset for that "pro" feel
                float distance = player.distanceTo(target) - 0.45f;
                if (distance < 0) distance = 0;
                reach.setLastReach(distance);
            }
        }
    }
}
