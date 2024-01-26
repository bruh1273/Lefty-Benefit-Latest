package org.benefit.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.benefit.LayoutPos;
import org.benefit.Variables;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static org.benefit.Client.mc;

@Mixin(ShulkerBoxScreen.class)
public abstract class ShulkerBoxScreenMixin extends HandledScreen<ShulkerBoxScreenHandler> {
    @Unique private TextFieldWidget textBox;
    public ShulkerBoxScreenMixin(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        //we're assuming that MinecraftClient.getInstance().player is never going to be null when this code is ran.
        assert mc.player != null;

        //create input text box
        textBox = new TextFieldWidget(mc.textRenderer, LayoutPos.xValue(100), LayoutPos.sendChatYPos(), 100, 20, Text.of("Send Chat"));
        textBox.setText(Variables.lastCommand);
        textBox.setMaxLength(65535);

        //render the send chat button
//        this.addDrawableChild(ButtonWidget.builder(Text.literal("Send chat"), button -> sendChat()).dimensions(5, 250, 80, 20).build());

        //render get name button
        this.addDrawableChild(ButtonWidget.builder(Text.of("Get Name"), button -> {
            //dispatch container's name to player in chat
            mc.player.sendMessage(Text.literal("Container Name: ").append(title));
            //automatically copy the title to clipboard when called
            mc.keyboard.setClipboard(title.getString());
        }).dimensions(LayoutPos.xValue(80), LayoutPos.getNameYPos(), 80, 20).build());
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

    /**
     * define calculations for the text box
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
        textBox.render(context, mouseX, mouseY, delta);
        if(!textBox.isFocused() && textBox.getText().isBlank()) textBox.setSuggestion("Send Chat...");
        if(textBox.isFocused()) textBox.setSuggestion("");
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
        if (textBox.isFocused()) {
            if(keyCode == GLFW.GLFW_KEY_ENTER) sendChat();
            if(this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) return false;
        }
        return textBox.keyPressed(keyCode, scanCode, m) || super.keyPressed(keyCode, scanCode, m);
    }

    /**
     * define rendering calculations for the text box
     */
    @Override
    public boolean mouseClicked(double mX, double mY, int b) {
        textBox.onClick(mX, mY);
        if(textBox.mouseClicked(mX, mY, b)) textBox.setFocused(true);
        if(!textBox.mouseClicked(mX, mY, b)) textBox.setFocused(false);
        return super.mouseClicked(mX, mY, b);
    }

    @Override
    public void removed() {
        Variables.lastCommand = textBox.getText();
    }
}
