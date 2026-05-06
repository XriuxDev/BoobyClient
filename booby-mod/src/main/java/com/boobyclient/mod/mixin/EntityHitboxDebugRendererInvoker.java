package com.boobyclient.mod.mixin;

import net.minecraft.client.render.debug.EntityHitboxDebugRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityHitboxDebugRenderer.class)
public interface EntityHitboxDebugRendererInvoker {
    @Invoker("drawHitbox")
    void boobyclient$drawHitbox(Entity entity, float tickProgress, boolean inLocalServer);
}
