package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class FullBrightMixin {

    @Inject(method = "getNightVisionStrength", at = @At("HEAD"), cancellable = true)
    private static void onGetNightVision(CallbackInfoReturnable<Float> cir) {
        if (BoobyMod.hudManager != null) {
            var module = BoobyMod.hudManager.getModule("fullbright");
            if (module != null && module.isEnabled()) {
                cir.setReturnValue(1.0f);
            }
        }
    }
}