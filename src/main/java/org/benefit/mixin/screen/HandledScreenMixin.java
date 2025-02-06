package org.benefit.mixin.screen;

import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import org.benefit.Benefit;
import org.benefit.LayoutMode;
import org.benefit.LayoutPos;
import org.benefit.Variables;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.benefit.Benefit.mc;
import static org.benefit.Benefit.restoreScreenBind;


@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int x, y;
    @Unique private TextFieldWidget textBox;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    // The main method
    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        assert mc.player != null;

        String boldGray = Formatting.BOLD.toString() + Formatting.GRAY, boldGreen = Formatting.BOLD.toString() + Formatting.GREEN;

        // Delay packets
        addDrawableChild(ButtonWidget.builder(Text.of("Delay packets: " + Variables.delayUIPackets), button -> {
            Variables.delayUIPackets = !Variables.delayUIPackets;

            button.setMessage(Text.of("Delay packets: " + Variables.delayUIPackets));

            if (!Variables.delayUIPackets && !Variables.delayedPackets.isEmpty()) {
                for (final Packet<?> packet : Variables.delayedPackets) {
                    mc.player.networkHandler.sendPacket(packet);
                }
                int delayedPacketsCount = Variables.delayedPackets.size();
                mc.player.sendMessage(Text.literal(boldGray + "Successfully sent " + boldGreen + delayedPacketsCount + Formatting.GRAY + " delayed packets."),false);
                Variables.delayedPackets.clear();
            }
        }).width(120).position(LayoutPos.xValue(120), LayoutPos.baseY() - 120).build());

        // Soft Close
        addDrawableChild(ButtonWidget.builder(Text.of("Soft Close"), button -> mc.setScreen(null))
                .width(80).position(LayoutPos.xValue(80), LayoutPos.baseY() - 150).build());

        // Save UI
        addDrawableChild(ButtonWidget.builder(Text.of("Save UI"), button -> {
            Variables.storedScreen = mc.currentScreen;
            Variables.storedScreenHandler = mc.player.currentScreenHandler;
            mc.setScreen(null);
            mc.player.sendMessage(Text.literal("Screen §asuccessfully §rsaved! Press §a" + restoreScreenBind.getString() + " §rto restore it!"),false);
        }).width(80).position(LayoutPos.xValue(80), LayoutPos.baseY() - 90).build());

        // Leave & send packets
        addDrawableChild(ButtonWidget.builder(Text.of("Leave & send packets"), button -> {
            if (!Variables.delayedPackets.isEmpty()) {

                Variables.delayUIPackets = false;

                for(final Packet<?> packet : Variables.delayedPackets) {
                    mc.player.networkHandler.sendPacket(packet);
                }
                int delayedPacketsAmount = Variables.delayedPackets.size();
                // Disconnect player
                Objects.requireNonNull(mc.getNetworkHandler()).getConnection().disconnect(
                        Text.of(boldGray + "Disconnected, " + boldGreen + delayedPacketsAmount + boldGray + " packets successfully sent."));
                Variables.delayedPackets.clear();
            }
        }).width(140).position(LayoutPos.xValue(140), LayoutPos.baseY() - 60).build());

        // Get Name
        if(inContainer()) addDrawableChild(ButtonWidget.builder(Text.of("Get Name"), button -> {
            boolean json = Benefit.config.shouldCopyJson();
            System.out.println(json);
            // We can use codecs to convert anything into a DynamicOp, default ones are NBT & JSON.
            String dfuParsed = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, title).getOrThrow(JsonParseException::new).toString();
            // Dispatch container's name to player in chat
            mc.player.sendMessage(Text.literal(json ? "Container JSON: " : "Container Name: ").append(json ? Text.of(dfuParsed) : title),false);
            // Automatically copy the title to clipboard when called
            mc.keyboard.setClipboard(json ? dfuParsed : title.getString());
        }).dimensions(LayoutPos.xValue(80), LayoutPos.getNameYPos(), 80, 20).build());

        // Paper Dupe (1.20.6 - 1.21.1)
        addDrawableChild(ButtonWidget.builder(Text.of("Paper Dupe"), button -> {
            if(!(mc.player.getInventory().getMainHandStack().getItem()  == Items.WRITABLE_BOOK)) {
                mc.player.sendMessage(Text.of("Please hold a writable book!"),false);
                return;
            }
            for(int i = 9; i < 44; i++) {
                if(36 + mc.player.getInventory().selectedSlot == i) continue;
                mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                        mc.player.currentScreenHandler.syncId,
                        mc.player.currentScreenHandler.getRevision(),
                        i,
                        1,
                        SlotActionType.THROW,
                        ItemStack.EMPTY,
                        Int2ObjectMaps.emptyMap()
                ));
            }
            mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(
                    mc.player.getInventory().selectedSlot, List.of("discord.gg/lefty"), Optional.of("Lefty Dupes On Fucking TOP discord.gg/lefty"
            )));
        }).dimensions(LayoutPos.xValue(80), LayoutPos.baseY() - 30, 80, 20).build());

        // Create input text box
        textBox = new TextFieldWidget(mc.textRenderer, LayoutPos.xValue(100), LayoutPos.sendChatYPos(), 100, 20, Text.of("Send Chat"));
        textBox.setText(Variables.lastCommand);
        textBox.setMaxLength(65535);
    }

    @Inject(at = @At("RETURN"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        assert client != null;

        // Slot Overlay
        if(Benefit.config.getOverlayValue()) {
            Benefit.addText(context, client.textRenderer, client, this.x, this.y);
        }

        // Sync ID and Revision
        Benefit.renderTexts(context, client.textRenderer, client);

        // Technical Handling
        if(inContainer()) textBox.render(context, mouseX, mouseY, delta);
        if(!textBox.isFocused() && textBox.getText().isBlank()) textBox.setSuggestion("Send Chat...");
        if(textBox.isFocused()) textBox.setSuggestion("");
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Release any alt key and the color of the slot overlay will go away.
        final int color = 0xFF828282;
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT && Benefit.txtColor != color) {
            Benefit.txtColor = color;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return textBox.charTyped(chr, keyCode) || super.charTyped(chr, keyCode);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        assert client != null;
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT && Benefit.txtColor != -1) {
            Benefit.txtColor = -1;
        }

        if (textBox.isFocused()) {
            if(keyCode == GLFW.GLFW_KEY_ENTER) sendChat();
            if(client.options.inventoryKey.matchesKey(keyCode, scanCode)) cir.setReturnValue(false);
            textBox.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Inject(at = @At("HEAD"), method = "mouseClicked")
    private void mouseClick(double mX, double mY, int b, CallbackInfoReturnable<Boolean> cir) {
        textBox.onClick(mX, mY);
        if(textBox.mouseClicked(mX, mY, b)) textBox.setFocused(true);
        if(!textBox.mouseClicked(mX, mY, b)) textBox.setFocused(false);
    }

    @Unique
    private void sendChat() {
        assert mc.player != null;
        if(Benefit.config.getLayoutMode() != LayoutMode.NONE) {
            // Send message
            String s = textBox.getText();
            if (s.startsWith("/")) mc.player.networkHandler.sendChatCommand(s.substring(1));
            else mc.player.networkHandler.sendChatMessage(s);

            // Reset state
            textBox.setText("");
            Variables.lastCommand = "";
        }
    }

    @Unique
    private boolean inContainer() {
        return mc.currentScreen instanceof Generic3x3ContainerScreen
                || mc.currentScreen instanceof GenericContainerScreen
                || mc.currentScreen instanceof ShulkerBoxScreen
                || mc.currentScreen instanceof HopperScreen;
    }


}