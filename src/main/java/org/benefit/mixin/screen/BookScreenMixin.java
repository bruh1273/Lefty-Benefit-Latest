package org.benefit.mixin.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.benefit.Client;
import org.benefit.LayoutPos;
import org.benefit.Variables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static org.benefit.Client.mc;
import static org.benefit.Client.restoreScreenBind;


@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {
    protected BookScreenMixin(Text title) {
        super(title);
    }

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
                for (Packet<?> packet : Variables.delayedPackets) Objects.requireNonNull(mc.getNetworkHandler()).sendPacket(packet);
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
        }).width(80).position(LayoutPos.xValue(80), LayoutPos.baseY() - 60).build());

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
                Objects.requireNonNull(mc.getNetworkHandler()).getConnection().disconnect(Text.of(bGray + "Disconnected, " + bGreen + DelayedPacketsAmount + bGray + " packets successfully sent."));
                Variables.delayedPackets.clear();
            }
        }).width(140).position(LayoutPos.xValue(140), LayoutPos.baseY() - 30).build());
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        assert client != null;

        // Add in Sync ID and Revision on screen.
        Client.renderTexts(context, client.textRenderer, client);
    }
}