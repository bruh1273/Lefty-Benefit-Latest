package org.benefit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.benefit.*;

@Mixin(GenericContainerScreen.class)
public abstract class ContainerScreenMixin extends HandledScreen<GenericContainerScreenHandler> implements ScreenHandlerProvider<GenericContainerScreenHandler> {
//define variables
    TextFieldWidget textBox;
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ClientPlayNetworkHandler mcp = mc.player.networkHandler;
    public ContainerScreenMixin(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    @Override
    protected void init() {
        super.init();
    //create input text box
            textBox = new TextFieldWidget(mc.textRenderer, 88, 5, 100, 20, Text.of("Command"));
            textBox.setText(Variables.lastCommand);
            textBox.setMaxLength(65535);
        //create send chat button
            ButtonWidget chatBtn = ButtonWidget.builder(Text.literal("Send chat"), button -> {
                String s = textBox.getText();
            //condition to see if the inputted text starts with a /, and if it doesn't, send a normal chat message instead of a command
                if (s.startsWith("/")) {
                    mcp.sendChatCommand(s.substring(1));
                } else { mcp.sendChatMessage(s); }
            }).dimensions(5, 5, 80, 20).build();
        //render the send chat button
            this.addDrawableChild(chatBtn);

    //add in getting the container's name button
        ButtonWidget NameButton = ButtonWidget.builder(Text.of("Get Name"), button -> {
        //dispatch container's name to player in chat
            mc.player.sendMessage(title);
        //automatically copy the title to clipboard when called
            String s = title.getString();
            mc.getInstance().keyboard.setClipboard(s);
        }).dimensions(4, 34, 80, 20).build();
    //render get name button
        this.addDrawableChild(NameButton);
    }
//define calculations for the text box
    @Override
    public void render(MatrixStack Matrices, int MouseX, int MouseY, float Delta) {
        this.renderBackground(Matrices);
        super.render(Matrices, MouseX, MouseY, Delta);
        this.drawMouseoverTooltip(Matrices, MouseX, MouseY);
            textBox.render(Matrices, MouseX, MouseY, Delta);
    }
//register when you type in the text box
    @Override
    public boolean charTyped(char chr, int keyCode) {
            textBox.charTyped(chr, keyCode);
            Variables.lastCommand = textBox.getText();
        return super.charTyped(chr, keyCode);
    }
//register when you type in the text box
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int m) {
            textBox.keyReleased(keyCode, scanCode, m);
            Variables.lastCommand = textBox.getText();
        return super.keyReleased(keyCode, scanCode, m);
    }
//register when you type in the text box
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int m) {
            textBox.keyPressed(keyCode, scanCode, m);
            Variables.lastCommand = textBox.getText();
        return super.keyPressed(keyCode, scanCode, m);
    }
//define rendering calculations for the text box
    @Override
    public boolean mouseClicked(double mX, double mY, int b) {
            textBox.mouseClicked(mX, mY, b);
            Variables.lastCommand = textBox.getText();
        return super.mouseClicked(mX, mY, b);
    }
}
//Huge thanks to Saturn5VFive for helping me out