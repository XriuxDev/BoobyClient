package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import com.boobyclient.hud.modules.ComboCounterModule;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class CombatMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        if (BoobyMod.hudManager != null) {
            ComboCounterModule combo = (ComboCounterModule) BoobyMod.hudManager.getModule("combo_counter");
            if (combo != null) {
                combo.onHit();
            }
        }
    }

    @Inject(method = "handleStatus", at = @At("HEAD"))
    private void onHandleStatus(byte status, CallbackInfo ci) {
        // Status 2 is "hurt" (damage taken)
        if (status == 2 && BoobyMod.hudManager != null) {
            ComboCounterModule combo = (ComboCounterModule) BoobyMod.hudManager.getModule("combo_counter");
            if (combo != null) {
                combo.resetCombo();
            }
        }
    }
}
