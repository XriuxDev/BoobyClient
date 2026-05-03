package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("RETURN"))
    public void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (BoobyMod.hudManager == null) {
            BoobyMod.initializeHUD(context.getScaledWindowWidth(), context.getScaledWindowHeight());
        } else {
            BoobyMod.hudManager.updateScreenSize(context.getScaledWindowWidth(), context.getScaledWindowHeight());
        }

        // Pass context to our custom renderer
        BoobyMod.hudManager.getRenderer().setContext(context);
        
        BoobyMod.hudManager.render();
    }
}
