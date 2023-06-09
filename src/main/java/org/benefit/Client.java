package org.benefit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import java.awt.*;

public class Client implements ClientModInitializer {
    public static KeyBinding restoreScreenKey;
    @Override
    public void onInitializeClient() {

        //add console text on game close
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\033[38;2;50;205;50mShutting Down Lefty's Benefit...\033[0m");
            System.out.println("\033[38;2;50;205;50mLefty's Benefit Successfully Shut Down!\033[0m");
        }));
        //add console text on game open
        System.out.println("\033[38;2;50;205;50mLefty Benefit Successfully Initialized, Thanks to Lefty Dupes!\033[0m");
        //register keybind for restoring screen
        restoreScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Restore Screen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "UI Utils"));
        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            while (restoreScreenKey.wasPressed()) {
                if (Variables.storedScreen != null && Variables.storedScreenHandler != null) {
                    client.setScreen(Variables.storedScreen);
                    client.player.currentScreenHandler = Variables.storedScreenHandler;}}
        });
    }
    public static void renderHandledScreen(MinecraftClient mc, TextRenderer textRenderer, MatrixStack matrices) {
        //define syncid and revision
        int syncID = mc.player.currentScreenHandler.syncId;
        int revision = mc.player.currentScreenHandler.getRevision();

        // display the current gui's sync id and revision
        textRenderer.draw(matrices, "Sync Id: " + syncID, 125, 35, Color.WHITE.getRGB());
        textRenderer.draw(matrices, "Revision: " + revision, 125, 65, Color.WHITE.getRGB());
    }
}