package com.boobyclient.hud.modules;

import com.boobyclient.hud.HUDModule;
import com.boobyclient.hud.HUDRenderer;

public class HitboxModule extends HUDModule {
    public HitboxModule() {
        super("hitbox", "Hitbox");
    }

    @Override
    public void render(HUDRenderer renderer) {
        // Rendered in-world by HitboxOverlayRenderer
    }
}
