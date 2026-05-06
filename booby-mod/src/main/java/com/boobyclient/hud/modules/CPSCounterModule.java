package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.Deque;

public class CPSCounterModule extends HUDModule {
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private int leftCps = 0;
    private int rightCps = 0;
    private boolean leftMouseWasPressed = false;
    private boolean rightMouseWasPressed = false;

    public CPSCounterModule() {
        super("cps_counter", "CPS Counter");
        this.x = 10;
        this.y = 145;
        this.width = 108;
        this.height = 20;
    }

    @Override
    public void render(HUDRenderer renderer) {
        if (!enabled) return;

        int glowColor = HUDRenderer.getColor(14, 165, 233, 110);
        int totalCps = leftCps + rightCps;
        int cpsColor = totalCps >= 10
                ? HUDRenderer.getColor(34, 197, 94)
                : HUDRenderer.getColor(226, 232, 240);

        renderer.drawModuleSurface(x - 4, y - 4, width + 10, 20, glowColor);
        renderer.drawText("CPS", x, y + 2, HUDRenderer.getColor(148, 163, 184), 0.7f);
        renderer.drawText("L " + leftCps + " R " + rightCps, x + 22, y, cpsColor, 0.85f);
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean leftMousePressed = client != null
                && client.options != null
                && client.options.attackKey.isPressed();
        boolean rightMousePressed = client != null
                && client.options != null
                && client.options.useKey.isPressed();

        // Count only rising edges so holding LMB doesn't inflate CPS.
        if (leftMousePressed && !leftMouseWasPressed) {
            registerLeftClick();
        }
        leftMouseWasPressed = leftMousePressed;
        if (rightMousePressed && !rightMouseWasPressed) {
            registerRightClick();
        }
        rightMouseWasPressed = rightMousePressed;

        long now = System.currentTimeMillis();
        while (!leftClicks.isEmpty() && now - leftClicks.peekFirst() > 1000L) {
            leftClicks.pollFirst();
        }
        while (!rightClicks.isEmpty() && now - rightClicks.peekFirst() > 1000L) {
            rightClicks.pollFirst();
        }
        leftCps = leftClicks.size();
        rightCps = rightClicks.size();
    }

    public void registerLeftClick() {
        leftClicks.addLast(System.currentTimeMillis());
    }

    public void registerRightClick() {
        rightClicks.addLast(System.currentTimeMillis());
    }
}
