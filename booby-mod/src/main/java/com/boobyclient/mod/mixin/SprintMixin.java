package com.boobyclient.mod.mixin;

import com.boobyclient.mod.BoobyMod;
import com.boobyclient.hud.modules.ToggleSprintModule;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class SprintMixin {

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void onTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        
        if (BoobyMod.hudManager != null) {
            ToggleSprintModule module = (ToggleSprintModule) BoobyMod.hudManager.getModule("toggle_sprint");
            if (module != null && module.isEnabled()) {
                // Force sprint if moving forward and not blocking conditions
                net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                if (client.options.forwardKey.isPressed() && !player.isSneaking() && !player.horizontalCollision) {
                    player.setSprinting(true);
                }
            }
        }
    }
}
