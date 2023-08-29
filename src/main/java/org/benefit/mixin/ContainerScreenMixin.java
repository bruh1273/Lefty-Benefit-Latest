package org.benefit.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.benefit.*;
import org.spongepowered.asm.mixin.Unique;

import static org.benefit.Client.mc;

/**
 * Huge thanks to Saturn5VFive for helping me out
 */
@Mixin(GenericContainerScreen.class)
public abstract class ContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> implements ScreenHandlerProvider<GenericContainerScreenHandler> {
    @Unique private TextFieldWidget textBox;

    protected ContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        //we're assuming that MinecraftClient.getInstance().player is never going to be null when this code is ran.
        assert mc.player != null;

        //create input text box
        textBox = new TextFieldWidget(mc.textRenderer, 88, 250, 100, 20, Text.of("Command"));
        textBox.setText(Variables.lastCommand);
        textBox.setMaxLength(65535);
        textBox.setFocused(true);

        //render the send chat button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Send chat"), button -> sendChat()).dimensions(5, 250, 80, 20).build());

        //render get name button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Get Name"), button -> {
            //dispatch container's name to player in chat
            mc.player.sendMessage(Text.literal("Container Name: ").append(title));
            //automatically copy the title to clipboard when called
            mc.keyboard.setClipboard(title.getString());
        }).dimensions(4, 220, 80, 20).build());
    }

    @Unique
    private void sendChat() {
        //we're assuming that MinecraftClient.getInstance().player is never going to be null when this code is ran.
        assert mc.player != null;

        //send message
        String s = textBox.getText();
        if (s.startsWith("/")) {
            mc.player.networkHandler.sendChatCommand(s.substring(1));
        } else mc.player.networkHandler.sendChatMessage(s);

        //reset state
        textBox.setText("");
        Variables.lastCommand = "";
    }

    /**
     * define calculations for the text box
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        textBox.render(context, mouseX, mouseY, delta);
    }

    /**
     * register when you type in the text box
     */
    @Override
    public boolean charTyped(char chr, int keyCode) {
        return textBox.charTyped(chr, keyCode) || super.charTyped(chr, keyCode);
    }

    /**
     * register when you type in the text box
     */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int m) {
        return textBox.keyReleased(keyCode, scanCode, m) || super.keyReleased(keyCode, scanCode, m);
    }

    /**
     * register when you type in the text box
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int m) {
        if (textBox.isFocused() && keyCode == GLFW.GLFW_KEY_ENTER) {
            sendChat();
            return true;
        } else return textBox.keyPressed(keyCode, scanCode, m) || super.keyPressed(keyCode, scanCode, m);
    }

    /**
     * define rendering calculations for the text box
     */
    @Override
    public boolean mouseClicked(double mX, double mY, int b) {
        return textBox.mouseClicked(mX, mY, b) || super.mouseClicked(mX, mY, b);
    }

    @Override
    public void removed() {
        Variables.lastCommand = textBox.getText();
    }
}