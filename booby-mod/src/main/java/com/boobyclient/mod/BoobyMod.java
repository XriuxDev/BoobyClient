package com.boobyclient.mod;

import com.boobyclient.hud.HUDManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoobyMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("booby-client");
    public static HUDManager hudManager;

    @Override
    public void onInitialize() {
        LOGGER.info("!!! BOOBY CLIENT MOD LOADED SUCCESSFULLY !!!");
        LOGGER.info("BoobyMod initialized.");
    }

    public static void initializeHUD(int width, int height) {
        if (hudManager == null) {
            hudManager = new HUDManager(width, height);
            LOGGER.info("Booby HUD initialized in-game");
        }
    }
}
