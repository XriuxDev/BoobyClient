package com.boobyclient.hud;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.KeyInput;

import java.util.ArrayList;
import java.util.List;

public class BoobyScreen extends Screen {
    private static final float MENU_W = 420.0f;
    private static final float MENU_H = 300.0f;
    private static final float HEADER_H = 42.0f;
    private static final float PADDING = 12.0f;
    private static final float CARD_H = 34.0f;
    private static final float CARD_GAP_X = 10.0f;
    private static final float CARD_GAP_Y = 8.0f;
    private static final float HEADER_TOGGLE_W = 88.0f;
    private static final float HEADER_TOGGLE_H = 16.0f;

    private final HUDManager hudManager;
    private final HUDRenderer renderer;
    private HUDModule draggingModule = null;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;
    private double scrollY = 0;

    public BoobyScreen(HUDManager hudManager) {
        super(Text.literal("Booby Client Menu"));
        this.hudManager = hudManager;
        this.renderer = hudManager.getRenderer();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderer.setContext(context);
        renderer.updateScreenSize(context.getScaledWindowWidth(), context.getScaledWindowHeight());

        int screenW = renderer.getScreenWidth();
        int screenH = renderer.getScreenHeight();

        for (HUDModule module : hudManager.getAllModules()) {
            if (module.isEnabled() && isDraggableModule(module)) {
                int color = module == draggingModule
                        ? HUDRenderer.getColor(34, 197, 94, 120)
                        : HUDRenderer.getColor(148, 163, 184, 55);
                renderer.drawRoundedRect(module.getX(), module.getY(), module.getWidth(), module.getHeight(), 2, color);
            }
        }

        float x = (screenW - MENU_W) * 0.5f;
        float y = (screenH - MENU_H) * 0.5f;

        renderer.drawGlow(x, y, MENU_W, MENU_H, 16, HUDRenderer.getColor(99, 102, 241, 90));
        renderer.drawRoundedRect(x, y, MENU_W, MENU_H, 14, HUDRenderer.getColor(15, 23, 42, 230));
        renderer.drawRoundedRect(x, y, MENU_W, HEADER_H, 14, HUDRenderer.getColor(99, 102, 241, 180));
        renderer.drawText("BOOBY CLIENT HUD", x + 14, y + 11, HUDRenderer.getColor(255, 255, 255), 1.0f);
        renderer.drawText("Toggle modules | Drag enabled HUD elements", x + 14, y + 25, HUDRenderer.getColor(226, 232, 240, 180), 0.68f);
        drawHeaderToggle(x, y);

        float contentX = x + PADDING;
        float contentY = y + HEADER_H + PADDING - (float) scrollY;
        float contentW = MENU_W - (PADDING * 2.0f);
        float columnW = (contentW - CARD_GAP_X) * 0.5f;
        float viewportTop = y + HEADER_H;
        float viewportBottom = y + MENU_H - PADDING;

        context.enableScissor((int) (x + PADDING), (int) viewportTop, (int) (x + MENU_W - PADDING), (int) (y + MENU_H - PADDING));
        List<HUDModule> modules = getModulesOrdered();
        for (int i = 0; i < modules.size(); i++) {
            HUDModule module = modules.get(i);
            int col = i % 2;
            int row = i / 2;
            float cardX = contentX + col * (columnW + CARD_GAP_X);
            float cardY = contentY + row * (CARD_H + CARD_GAP_Y);

            if (cardY + CARD_H < viewportTop || cardY > viewportBottom) {
                continue;
            }

            boolean enabled = module.isEnabled();
            int cardColor = enabled ? HUDRenderer.getColor(30, 41, 59, 205) : HUDRenderer.getColor(30, 41, 59, 150);
            int statusColor = enabled ? HUDRenderer.getColor(34, 197, 94) : HUDRenderer.getColor(239, 68, 68);

            renderer.drawRoundedRect(cardX, cardY, columnW, CARD_H, 8, cardColor);
            renderer.drawText(module.getDisplayName(), cardX + 10, cardY + 12, HUDRenderer.getColor(226, 232, 240), 0.85f);

            float switchW = 32.0f;
            float switchH = 14.0f;
            float switchX = cardX + columnW - switchW - 8.0f;
            float switchY = cardY + (CARD_H - switchH) * 0.5f;
            renderer.drawRoundedRect(switchX, switchY, switchW, switchH, 7, HUDRenderer.getColor(15, 23, 42));
            renderer.drawRoundedRect(enabled ? switchX + 17 : switchX + 3, switchY + 2, 12, 10, 5, statusColor);
        }
        context.disableScissor();

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        if (button == 0) {
            for (HUDModule module : hudManager.getAllModules()) {
                if (module.isEnabled() && isDraggableModule(module) && module.isHovered((float) mouseX, (float) mouseY)) {
                    draggingModule = module;
                    dragOffsetX = (float) mouseX - module.getX();
                    dragOffsetY = (float) mouseY - module.getY();
                    return true;
                }
            }

            int screenW = renderer.getScreenWidth();
            int screenH = renderer.getScreenHeight();
            float x = (screenW - MENU_W) * 0.5f;
            float y = (screenH - MENU_H) * 0.5f;

            if (isInsideHeaderToggle(mouseX, mouseY, x, y)) {
                HUDRenderer.setModuleBackgroundsEnabled(!HUDRenderer.isModuleBackgroundsEnabled());
                hudManager.saveConfig();
                return true;
            }

            float contentX = x + PADDING;
            float contentY = y + HEADER_H + PADDING - (float) scrollY;
            float contentW = MENU_W - (PADDING * 2.0f);
            float columnW = (contentW - CARD_GAP_X) * 0.5f;
            float viewportTop = y + HEADER_H;
            float viewportBottom = y + MENU_H - PADDING;

            List<HUDModule> modules = getModulesOrdered();
            for (int i = 0; i < modules.size(); i++) {
                HUDModule module = modules.get(i);
                int col = i % 2;
                int row = i / 2;
                float cardX = contentX + col * (columnW + CARD_GAP_X);
                float cardY = contentY + row * (CARD_H + CARD_GAP_Y);

                if (cardY + CARD_H < viewportTop || cardY > viewportBottom) {
                    continue;
                }

                if (mouseX >= cardX && mouseX <= cardX + columnW && mouseY >= cardY && mouseY <= cardY + CARD_H) {
                    module.setEnabled(!module.isEnabled());
                    hudManager.saveConfig();
                    return true;
                }
            }
        }
        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollY -= verticalAmount * 10;
        if (scrollY < 0) {
            scrollY = 0;
        }
        if (scrollY > getMaxScroll()) {
            scrollY = getMaxScroll();
        }
        return true;
    }

    private double getMaxScroll() {
        int moduleCount = getModulesOrdered().size();
        int rows = (int) Math.ceil(moduleCount / 2.0);
        float totalHeight = rows <= 0 ? 0.0f : (rows * CARD_H) + ((rows - 1) * CARD_GAP_Y);
        float viewportHeight = MENU_H - HEADER_H - (PADDING * 2.0f);
        return Math.max(0.0f, totalHeight - viewportHeight);
    }

    @Override
    public boolean mouseReleased(Click click) {
        int button = click.button();
        if (button == 0 && draggingModule != null) {
            draggingModule = null;
            hudManager.saveConfig();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double dragX, double dragY) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (draggingModule != null) {
            draggingModule.setX((float) mouseX - dragOffsetX);
            draggingModule.setY((float) mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(click, dragX, dragY);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        for (HUDModule module : hudManager.getAllModules()) {
            module.onInput(input.key(), input.scancode(), 1);
        }
        return super.keyPressed(input);
    }

    private List<HUDModule> getModulesOrdered() {
        return new ArrayList<>(hudManager.getAllModules());
    }

    private boolean isDraggableModule(HUDModule module) {
        String id = module.getModuleId();
        return !"zoom".equals(id) && !"fullbright".equals(id);
    }

    private void drawHeaderToggle(float x, float y) {
        float toggleX = x + MENU_W - HEADER_TOGGLE_W - 12.0f;
        float toggleY = y + 12.0f;
        boolean enabled = HUDRenderer.isModuleBackgroundsEnabled();
        int chipColor = enabled ? HUDRenderer.getColor(34, 197, 94, 210) : HUDRenderer.getColor(51, 65, 85, 210);
        renderer.drawRoundedRect(toggleX, toggleY, HEADER_TOGGLE_W, HEADER_TOGGLE_H, 6, HUDRenderer.getColor(15, 23, 42, 170));
        renderer.drawRoundedRect(toggleX + 2, toggleY + 2, 28, HEADER_TOGGLE_H - 4, 4, chipColor);
        renderer.drawText("Text only", toggleX + 34, toggleY + 4, HUDRenderer.getColor(226, 232, 240), 0.62f);
    }

    private boolean isInsideHeaderToggle(double mouseX, double mouseY, float x, float y) {
        float toggleX = x + MENU_W - HEADER_TOGGLE_W - 12.0f;
        float toggleY = y + 12.0f;
        return mouseX >= toggleX && mouseX <= toggleX + HEADER_TOGGLE_W
                && mouseY >= toggleY && mouseY <= toggleY + HEADER_TOGGLE_H;
    }
}
