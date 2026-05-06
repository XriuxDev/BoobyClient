package com.boobyclient.mod;

import net.minecraft.client.MinecraftClient;

public class ZoomHelper {
    public static boolean zoomed = false;
    public static int originalFov = 70;
    public static double zoomFov = 15.0;
    public static boolean fovApplied = false;
    private static final double MIN_ZOOM_FOV = 5.0;
    private static final double MAX_ZOOM_FOV = 70.0;
    private static final double ZOOM_SCROLL_SENSITIVITY = 0.88;

    public static void onScroll(double scrollDelta) {
        if (!zoomed) return;
        if (scrollDelta == 0.0D) return;

        // Exponential scaling gives consistent feel across all zoom levels.
        zoomFov *= Math.pow(ZOOM_SCROLL_SENSITIVITY, scrollDelta);
        zoomFov = Math.max(MIN_ZOOM_FOV, Math.min(MAX_ZOOM_FOV, zoomFov));
    }

    public static void startZoom() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        zoomed = true;
        if (!fovApplied) {
            originalFov = client.options.getFov().getValue();
            fovApplied = true;
        }
    }

    public static void applyZoom() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        if (!fovApplied) startZoom();
        client.options.getFov().setValue((int) Math.round(zoomFov));
    }

    public static void removeZoom() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;
        client.options.getFov().setValue(originalFov);
        fovApplied = false;
        zoomed = false;
    }
}