package com.boobyclient.mod;

import net.minecraft.client.MinecraftClient;

public class FullBrightHelper {
    private static boolean gammaApplied = false;
    private static double originalGamma = 1.0;
    private static final double FULLBRIGHT_GAMMA = 16.0;

    public static void apply() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return;

        if (!gammaApplied) {
            originalGamma = client.options.getGamma().getValue();
            gammaApplied = true;
        }

        client.options.getGamma().setValue(FULLBRIGHT_GAMMA);
    }

    public static void remove() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || !gammaApplied) return;

        client.options.getGamma().setValue(originalGamma);
        gammaApplied = false;
    }
}
