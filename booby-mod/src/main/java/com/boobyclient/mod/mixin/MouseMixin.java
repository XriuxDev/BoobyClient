package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import com.boobyclient.mod.ZoomHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        boolean zoomEnabled = false;
        if (BoobyMod.hudManager != null) {
            var zoomModule = BoobyMod.hudManager.getModule("zoom");
            zoomEnabled = zoomModule != null && zoomModule.isEnabled();
        }

        MinecraftClient client = MinecraftClient.getInstance();
        boolean cKeyDown = client != null
                && client.getWindow() != null
                && GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS;

        if (zoomEnabled && cKeyDown && vertical != 0.0D) {
            if (!ZoomHelper.zoomed) {
                ZoomHelper.startZoom();
            }
            ZoomHelper.onScroll(vertical);
            ZoomHelper.applyZoom();
            ci.cancel();
        }
    }

}