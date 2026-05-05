package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import net.minecraft.client.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Keyboard.class, remap = false)
public class KeyboardMixin {

    @Inject(method = "method_1466", at = @At("HEAD"), cancellable = true, remap = false)
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (BoobyMod.hudManager != null) {
            BoobyMod.hudManager.handleInput(key, scancode, action);
        }
    }
}
