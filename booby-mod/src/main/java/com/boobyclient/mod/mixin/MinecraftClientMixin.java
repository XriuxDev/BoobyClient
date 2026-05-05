package com.boobyclient.mod.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    
    @Inject(method = "method_44645", at = @At("HEAD"), cancellable = true, remap = false)
    private void onGetVersionType(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("release");
    }
}
