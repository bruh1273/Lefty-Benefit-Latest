package org.benefit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class Client implements ClientModInitializer {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static Text restoreScreenBind;
    public static int txtColor = 0xFF828282;

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
        KeyBinding restoreScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("benefit.key.restoreScreen", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "Benefit"));

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            //we're assuming that client.player is never going to be equal to null when this code is ran.
            //this assert statement will not impact your game at all unless you have the JVM flag -ea or -enableassertions enabled.
            assert client.player != null;
            restoreScreenBind = restoreScreenKey.getBoundKeyLocalizedText();

            if (restoreScreenKey.wasPressed() && Variables.storedScreen != null && Variables.storedScreenHandler != null) {
                client.setScreen(Variables.storedScreen);
                client.player.currentScreenHandler = Variables.storedScreenHandler;
            }
        });
    }

    public static void addText(DrawContext context, TextRenderer textRenderer, MinecraftClient client, int x, int y) {
        for(Slot slot : client.player.currentScreenHandler.slots) {
            Text id = Text.literal(""+slot.id);
            context.drawText(
                    textRenderer,
                    id,
                    (slot.x + x + 16 / 2 - textRenderer.getWidth(id) / 2),
                    (slot.y + y + 16 / 2 - textRenderer.fontHeight / 2),
                    txtColor,
                    false
            );
        }
    }
}