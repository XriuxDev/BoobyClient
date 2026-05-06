package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;

public class FullBrightModule extends HUDModule {

    public FullBrightModule() {
        super("fullbright", "FullBright");
    }

    @Override
    public void render(HUDRenderer renderer) {
        // Don't render anything - fullbright is handled by mixin
    }
}
