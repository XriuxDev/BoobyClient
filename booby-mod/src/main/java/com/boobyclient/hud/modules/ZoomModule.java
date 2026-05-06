package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;

public class ZoomModule extends HUDModule {

    public ZoomModule() {
        super("zoom", "Zoom");
    }

    @Override
    public void render(HUDRenderer renderer) {
        // Don't render anything - zoom is controlled via C key
    }
}
