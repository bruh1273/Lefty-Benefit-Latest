package org.benefit;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Benefit implements ClientModInitializer {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final Config config = new Config();
    public static Text restoreScreenBind;
    public static int txtColor = 0xFF828282;

    @Override
    public void onInitializeClient() {

        // Add console text on game close
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
        if(client.player == null) return;
        for (int i = 0; i < client.player.currentScreenHandler.slots.size(); i++) {
            final Slot slot = client.player.currentScreenHandler.slots.get(i);
            final Text id = Text.literal(Integer.toString(slot.id));
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

    public static void renderTexts(DrawContext context, TextRenderer textRenderer, MinecraftClient client) {
        if(client.player == null) return;
        final Text syncId = Text.of("Sync Id: " + client.player.currentScreenHandler.syncId);
        final Text revision = Text.of("Revision: " + client.player.currentScreenHandler.getRevision());
        context.drawText(textRenderer, syncId, x(client, textRenderer, syncId), y(true), -1, false);
        context.drawText(textRenderer, revision, x(client, textRenderer, syncId), y(false), -1, false);
    }

    private static int x(MinecraftClient client, TextRenderer renderer, Text text) {
        final int topRight = client.getWindow().getScaledWidth() - renderer.getWidth(text) - 4;
        final int bottomRight = (client.getWindow().getScaledWidth() - 82) - (renderer.getWidth(text) + 4);
        return switch(config.getLayoutMode()) {
            case TOP_LEFT -> 4;
            case TOP_RIGHT -> topRight;
            case BOTTOM_LEFT -> 88;
            case BOTTOM_RIGHT -> bottomRight;
            case NONE -> 9999;
        };
    }

    private static int y(boolean syncId) {
        return switch(config.getLayoutMode()) {
            case TOP_LEFT, TOP_RIGHT -> syncId ? 5 : 20;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> LayoutPos.baseY() - (syncId ? 79 : 89);
            case NONE -> 9999;
        };
    }

    public static final SimpleOption<LayoutMode> format = new SimpleOption<>("benefit.format", SimpleOption.constantTooltip(Text.translatable("benefit.format.tooltip")), (optionText, value) ->
            Text.translatable(value.getTranslationKey()),
            new SimpleOption.AlternateValuesSupportingCyclingCallbacks<>(
                    Arrays.asList(LayoutMode.values()),
                    Stream.of(LayoutMode.values()).collect(Collectors.toList()),
                    mc::isRunning,
                    SimpleOption::setValue,
                    Codec.INT.xmap(LayoutMode::byId, LayoutMode::getId)),
            config.getLayoutMode(), value -> {
                config.setLayout(value);
                config.save();
    });

    public static final SimpleOption<Boolean> overlay = SimpleOption.ofBoolean("benefit.overlay", SimpleOption.constantTooltip(Text.translatable("benefit.overlay.tooltip")), config.getOverlayValue(), value -> {
        config.setOverlayValue(value);
        config.save();
    });


}