package org.benefit.mixin.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
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

import java.util.Objects;

import static org.benefit.Benefit.mc;
import static org.benefit.Benefit.restoreScreenBind;


@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int x;

    @Shadow protected int y;
    @Unique private TextFieldWidget textBox;

    protected HandledScreenMixin(Text title) {
        super(title);
    }

    //the main method
    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        //we're assuming that MinecraftClient.getInstance().player is never going to be equal to null when this code is ran.
        //this assert statement will not impact your game at all unless you have the JVM flag -ea or -enableassertions enabled.
        assert mc.player != null;

        //simplify expressions
        String bGray = Formatting.BOLD.toString() + Formatting.GRAY;
        String bGreen = Formatting.BOLD.toString() + Formatting.GREEN;

        //add in delay packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Delay packets: " + Variables.delayUIPackets), (button) -> {
            Variables.delayUIPackets = !Variables.delayUIPackets;

            //setting the text on the button to true or false when it is active
            button.setMessage(Text.of("Delay packets: " + Variables.delayUIPackets));

            //condition to see if any delayed packets was delayed, then send them
            if (!Variables.delayUIPackets && !Variables.delayedPackets.isEmpty()) {
                for (Packet<?> packet : Variables.delayedPackets)
                    Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packet);


                //add in message to say how many delayed packets were sent
                int DelayedPacketsCount = Variables.delayedPackets.size();
                mc.player.sendMessage(Text.of(bGray + "Successfully sent " + bGreen + DelayedPacketsCount + Formatting.GRAY + " delayed packets."));
                Variables.delayedPackets.clear();
            }
        }).width(120).position(LayoutPos.xValue(120), LayoutPos.baseY() - 120).build());

        //add in softclose button
        addDrawableChild(ButtonWidget.builder(Text.of("Soft Close"), (button) -> mc.setScreen(null))
                .width(80).position(LayoutPos.xValue(80), LayoutPos.baseY() - 150).build());

        //add in save ui button
        addDrawableChild(ButtonWidget.builder(Text.of("Save UI"), (button) -> {
            //define variables
            Variables.storedScreen = mc.currentScreen;
            Variables.storedScreenHandler = mc.player.currentScreenHandler;
            mc.setScreen(null);
            mc.player.sendMessage(Text.literal("Screen §asuccessfully §rsaved! Press §a" + restoreScreenBind.getString() + " §rto restore it!"));
        }).width(80).position(LayoutPos.xValue(80), LayoutPos.baseY() - 90).build());

        //add in leave n send packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Leave & send packets"), (button) -> {

            if (!Variables.delayedPackets.isEmpty()) {
                Variables.delayUIPackets = false;

                for (Packet<?> packet : Variables.delayedPackets) {
                    Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packet);
                }
                //add in message to say how many delayed packets were sent
                int DelayedPacketsAmount = Variables.delayedPackets.size();

                //disconnect player
                Objects.requireNonNull(mc.getNetworkHandler()).getConnection().disconnect(
                        Text.of(bGray + "Disconnected, " + bGreen + DelayedPacketsAmount + bGray + " packets successfully sent."));
                Variables.delayedPackets.clear();
            }
        }).width(140).position(LayoutPos.xValue(140), LayoutPos.baseY() - 60).build());

        //create input text box
        textBox = new TextFieldWidget(mc.textRenderer, LayoutPos.xValue(100), LayoutPos.sendChatYPos(), 100, 20, Text.of("Send Chat"));
        textBox.setText(Variables.lastCommand);
        textBox.setMaxLength(65535);

        //render get name button
        if(inContainer()) this.addDrawableChild(ButtonWidget.builder(Text.of("Get Name"), button -> {
            //dispatch container's name to player in chat
            mc.player.sendMessage(Text.literal("Container Name: ").append(title));
            //automatically copy the title to clipboard when called
            mc.keyboard.setClipboard(title.getString());
        }).dimensions(LayoutPos.xValue(80), LayoutPos.getNameYPos(), 80, 20).build());
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        assert client != null;

        // Add in slot overlay
        if(Benefit.config.getLayoutMode() != LayoutMode.NONE && Benefit.config.getOverlayValue()) {
            Benefit.addText(context, client.textRenderer, client, this.x, this.y);
        }

        // Add in Sync ID and Revision on screen.
        Benefit.renderTexts(context, client.textRenderer, client);


        if(inContainer()) textBox.render(context, mouseX, mouseY, delta);
        if(!textBox.isFocused() && textBox.getText().isBlank()) textBox.setSuggestion("Send Chat...");
        if(textBox.isFocused()) textBox.setSuggestion("");
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Release any alt key and the color of the slot overlay will go away.
        final int clr = 0xFF828282;
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT && Benefit.txtColor != clr)
            Benefit.txtColor = clr;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        return textBox.charTyped(chr, keyCode) || super.charTyped(chr, keyCode);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
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
    public void mouseClick(double mX, double mY, int b, CallbackInfoReturnable<Boolean> cir) {
        textBox.onClick(mX, mY);
        if(textBox.mouseClicked(mX, mY, b)) textBox.setFocused(true);
        if(!textBox.mouseClicked(mX, mY, b)) textBox.setFocused(false);
    }

    @Unique
    private void sendChat() {
        //we're assuming that MinecraftClient.getInstance().player is never going to be null when this code is ran.
        assert mc.player != null;

        //send message
        String s = textBox.getText();
        if (s.startsWith("/")) mc.player.networkHandler.sendChatCommand(s.substring(1));
        else mc.player.networkHandler.sendChatMessage(s);

        //reset state
        textBox.setText("");
        Variables.lastCommand = "";
    }

    @Unique
    private boolean inContainer() {
        return mc.currentScreen instanceof Generic3x3ContainerScreen
                || mc.currentScreen instanceof GenericContainerScreen
                || mc.currentScreen instanceof ShulkerBoxScreen
                || mc.currentScreen instanceof HopperScreen;
    }
}