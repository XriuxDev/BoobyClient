package com.boobyclient.hud;

/**
 * Base class for HUD modules
 */
public abstract class HUDModule {
    protected String moduleId;
    protected String displayName;
    protected boolean enabled;
    protected float x, y;
    protected float width = 60, height = 20;
    protected float scale;

    public HUDModule(String moduleId, String displayName) {
        this.moduleId = moduleId;
        this.displayName = displayName;
        this.enabled = true;
        this.x = 10;
        this.y = 10;
        this.scale = 1.0f;
    }

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x - 4 && mouseX <= x + width + 4 && mouseY >= y - 4 && mouseY <= y + height + 4;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    /**
     * Called every frame to render the HUD element
     */
    public abstract void render(HUDRenderer renderer);

    /**
     * Called on each game tick for logic updates
     */
    public void tick() {
    }

    /**
     * Called when player input is detected
     */
    public void onInput(int keyCode, int scanCode, int action) {
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
