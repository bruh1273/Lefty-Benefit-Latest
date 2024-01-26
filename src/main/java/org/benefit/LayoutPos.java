package org.benefit;

import net.minecraft.client.util.Window;

public class LayoutPos {
    private static final Window win = Client.mc.getWindow();
    public static int baseY() {
        return switch(Client.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 190;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() + 36;
            case NONE -> 9999;
        };
    }
    public static int xValue(int width) {
        return switch(Client.config.getLayoutMode()) {
            case TOP_LEFT, BOTTOM_LEFT -> 4;
            case TOP_RIGHT, BOTTOM_RIGHT -> win.getScaledWidth() - width - 4;
            case NONE -> 9999;
        };
    }
    public static int sendChatYPos() {
        return switch(Client.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 190;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() - 174;
            case NONE -> 9999;
        };
    }
    public static int getNameYPos() {
        return switch(Client.config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> 160;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> win.getScaledHeight() - 144;
            case NONE -> 9999;
        };
    }
}
