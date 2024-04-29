package org.benefit.mixin.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {

    @Shadow protected TextFieldWidget chatField;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void drawChars(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        assert client != null;

        int length = chatField.getText().length();
        // Display the amount of characters in the chat box.
        context.drawText(client.textRenderer, Integer.toString(length), 4, client.getWindow().getScaledHeight() - 36,
                length > 256 ? 0xFFC38C8C : 0xFF7F7F7F,
        false);
    }

}