package com.boobyclient.mod.mixin;

import com.boobyclient.hud.BoobyScreen;
import com.boobyclient.mod.BoobyMod;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Keyboard.class)
public class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int action, net.minecraft.client.input.KeyInput input, CallbackInfo ci) {
        if (input.key() == 344 && action == 1) { // Right Shift
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen == null && BoobyMod.hudManager != null) {
                client.execute(() -> client.setScreen(new BoobyScreen(BoobyMod.hudManager)));
            }
        }
    }
}
