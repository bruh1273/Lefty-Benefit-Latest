package org.benefit;

import net.minecraft.client.util.Window;
import net.minecraft.text.Text;

public class LayoutPos {
    private static final Window win = Benefit.mc.getWindow();
    public static int baseY() {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 190;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() + 6;
            case NONE -> 9999;
        };
    }
    public static int signBaseY() {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 54;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() - 28;
            case NONE -> 9999;
        };
    }
    public static int xValue(int width) {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, BOTTOM_LEFT -> 4;
            case TOP_RIGHT, BOTTOM_RIGHT -> win.getScaledWidth() - width - 4;
            case NONE -> 9999;
        };
    }
    public static int sendChatYPos() {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 220;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() - 204;
            case NONE -> 9999;
        };
    }
    public static int getNameYPos() {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 190;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() - 174;
            case NONE -> 9999;
        };
    }
    public static int windowIdY(boolean syncId) {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> syncId ? 5 : 20;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> LayoutPos.baseY() - (syncId ? 79 : 89);
            case NONE -> 9999;
        };
    }
    public static int windowIdX(Text text) {
        final int topRight = Benefit.mc.getWindow().getScaledWidth() - Benefit.mc.textRenderer.getWidth(text) - 4,
                bottomRight = (Benefit.mc.getWindow().getScaledWidth() - 82) - (Benefit.mc.textRenderer.getWidth(text) + 4);
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT -> 4;
            case TOP_RIGHT -> topRight;
            case BOTTOM_LEFT -> 88;
            case BOTTOM_RIGHT -> bottomRight;
            case NONE -> 9999;
        };
    }
    public static int signPosY() {
        return switch(Benefit.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 78;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> LayoutPos.signBaseY() - 64;
            case NONE -> 9999;
        };
    }
}