package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import com.boobyclient.mod.FullBrightHelper;
import com.boobyclient.mod.ZoomHelper;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ZoomMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        boolean zoomEnabled = false;
        boolean fullbrightEnabled = false;
        if (BoobyMod.hudManager != null) {
            var zoomModule = BoobyMod.hudManager.getModule("zoom");
            var fullbrightModule = BoobyMod.hudManager.getModule("fullbright");
            zoomEnabled = zoomModule != null && zoomModule.isEnabled();
            fullbrightEnabled = fullbrightModule != null && fullbrightModule.isEnabled();
        }

        if (fullbrightEnabled && client.player != null) {
            FullBrightHelper.apply();
        } else {
            FullBrightHelper.remove();
        }

        if (client.player != null && client.currentScreen == null && client.getWindow() != null && zoomEnabled) {
            boolean cKeyDown = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;

            if (cKeyDown) {
                ZoomHelper.startZoom();
                ZoomHelper.applyZoom(); // Apply every tick so FOV stays locked
            } else if (ZoomHelper.zoomed) {
                ZoomHelper.removeZoom();
            }
        } else if (ZoomHelper.zoomed) {
            ZoomHelper.removeZoom();
        }
    }
}