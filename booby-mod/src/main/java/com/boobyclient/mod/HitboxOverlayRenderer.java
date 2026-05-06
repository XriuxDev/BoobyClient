package com.boobyclient.mod;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HitboxOverlayRenderer {

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null || BoobyMod.hudManager == null) return;

        var module = BoobyMod.hudManager.getModule("hitbox");
        if (module == null || !module.isEnabled()) return;

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider vertices = context.consumers();
        Vec3d cameraPos = client.player.getEyePos();

        for (Entity entity : client.world.getEntities()) {
            if (entity == client.player) continue;
            if (!(entity instanceof PlayerEntity) && !(entity instanceof MobEntity)) continue;

            Box box = entity.getBoundingBox();
            drawBox(matrices, vertices, box.offset(-cameraPos.x, -cameraPos.y, -cameraPos.z));
        }
    }

    private static void drawBox(MatrixStack matrices, VertexConsumerProvider vertices, Box box) {
        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertices.getBuffer(RenderLayers.LINES);
        
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        float r = 1.0f; // White
        float g = 1.0f;
        float b = 1.0f;
        float a = 1.0f;

        // Draw bottom edges
        drawLine(buffer, positionMatrix, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        drawLine(buffer, positionMatrix, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        drawLine(buffer, positionMatrix, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        drawLine(buffer, positionMatrix, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        // Draw top edges
        drawLine(buffer, positionMatrix, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(buffer, positionMatrix, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(buffer, positionMatrix, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        drawLine(buffer, positionMatrix, minX, maxY, maxZ, minX, maxY, minZ, r, g, b, a);

        // Draw vertical edges
        drawLine(buffer, positionMatrix, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        drawLine(buffer, positionMatrix, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(buffer, positionMatrix, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(buffer, positionMatrix, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void drawLine(VertexConsumer buffer, Matrix4f positionMatrix,
                                float x1, float y1, float z1, float x2, float y2, float z2,
                                float r, float g, float b, float a) {
        buffer.vertex(positionMatrix, x1, y1, z1).color(r, g, b, a);
        buffer.vertex(positionMatrix, x2, y2, z2).color(r, g, b, a);
    }
}
