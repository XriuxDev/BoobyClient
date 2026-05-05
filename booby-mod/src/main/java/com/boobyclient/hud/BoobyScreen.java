package com.boobyclient.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Click;

public class BoobyScreen extends Screen {
    private final HUDManager hudManager;
    private final HUDRenderer renderer;
    private HUDModule draggingModule = null;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;

    public BoobyScreen(HUDManager hudManager) {
        super(Text.literal("Booby Client Menu"));
        this.hudManager = hudManager;
        this.renderer = hudManager.getRenderer();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // We do NOT call super.renderBackground(context, mouseX, mouseY, delta); to allow game to show behind
        
        renderer.setContext(context);
        renderer.updateScreenSize(context.getScaledWindowWidth(), context.getScaledWindowHeight());
        
        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();
        
        // Draw the modules so they show underneath the menu or we can drag them
        // Actually, the main game hud already renders them, but we might want bounding boxes
        for (HUDModule module : hudManager.getAllModules()) {
            if (module.isEnabled()) {
                // Draw a dashed or faint bounding box if hover or moving
                int color = module == draggingModule ? HUDRenderer.getColor(255, 255, 0, 100) : HUDRenderer.getColor(255, 255, 255, 50);
                renderer.drawRoundedRect(module.getX(), module.getY(), module.getWidth(), module.getHeight(), 2, color);
            }
        }

        // Draw Menu Overlay
        float menuW = 320;
        float menuH = 240;
        float x = (screenW / 2f) - (menuW / 2f);
        float y = (screenH / 2f) - (menuH / 2f);
        
        renderer.drawGlow(x, y, menuW, menuH, 15, HUDRenderer.getColor(99, 102, 241, 80));
        renderer.drawRoundedRect(x, y, menuW, menuH, 12, HUDRenderer.getColor(15, 23, 42, 230));
        renderer.drawRoundedRect(x, y, menuW, 35, 12, HUDRenderer.getColor(99, 102, 241, 180));
        renderer.drawText("BOOBY CLIENT", x + 15, y + 10, HUDRenderer.getColor(255, 255, 255), 1.0f);
        renderer.drawText("MOD SETTINGS (Drag HUD elements to move)", x + 15, y + 22, HUDRenderer.getColor(200, 200, 200, 150), 0.6f);
        
        float yOffset = 50;
        for (HUDModule module : hudManager.getAllModules()) {
            boolean enabled = module.isEnabled();
            int statusColor = enabled ? HUDRenderer.getColor(34, 197, 94) : HUDRenderer.getColor(239, 68, 68);
            renderer.drawRoundedRect(x + 10, y + yOffset, menuW - 20, 35, 8, HUDRenderer.getColor(30, 41, 59, 180));
            renderer.drawText(module.getDisplayName(), x + 25, y + yOffset + 12, HUDRenderer.getColor(226, 232, 240), 0.9f);
            renderer.drawRoundedRect(x + menuW - 60, y + yOffset + 10, 35, 15, 7, HUDRenderer.getColor(15, 23, 42));
            renderer.drawRoundedRect(x + menuW - (enabled ? 40 : 58), y + yOffset + 12, 11, 11, 5, statusColor);
            yOffset += 42;
        }
        
        super.render(context, mouseX, mouseY, delta);
    }
    
    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button == 0) {
            // Check Modules for dragging first
            for (HUDModule module : hudManager.getAllModules()) {
                if (module.isEnabled() && module.isHovered((float)mouseX, (float)mouseY)) {
                    draggingModule = module;
                    dragOffsetX = (float)mouseX - module.getX();
                    dragOffsetY = (float)mouseY - module.getY();
                    return true;
                }
            }

            // Check Menu toggles - Use EXACT SAME math as render()
            int screenW = renderer.getScreenWidth();
            int screenH = renderer.getScreenHeight();
            float menuW = 320;
            float x = (screenW / 2f) - (menuW / 2f);
            float y = (screenH / 2f) - (240 / 2f);
            
            float yOffset = 50;
            for (HUDModule module : hudManager.getAllModules()) {
                float boxX = x + 10;
                float boxY = y + yOffset;
                float boxW = menuW - 20;
                float boxH = 35;
                
                if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH) {
                    module.setEnabled(!module.isEnabled());
                    hudManager.saveConfig(); // Auto-save on toggle
                    return true;
                }
                yOffset += 42;
            }
        }
        return super.mouseClicked(click, bl);
    }
    
    @Override
    public boolean mouseReleased(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button == 0 && draggingModule != null) {
            draggingModule = null;
            hudManager.saveConfig(); // Auto-save on release
            return true;
        }
        return super.mouseReleased(click);
    }
    
    @Override
    public boolean mouseDragged(Click click, double dragX, double dragY) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (draggingModule != null) {
            draggingModule.setX((float)mouseX - dragOffsetX);
            draggingModule.setY((float)mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
