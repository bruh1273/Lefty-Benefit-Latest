package org.benefit.mixin.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.benefit.Benefit;
import org.benefit.LayoutPos;
import org.benefit.Variables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static org.benefit.Benefit.mc;
import static org.benefit.Benefit.restoreScreenBind;


@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {
    protected BookScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        assert mc.player != null;

        String boldGray = Formatting.BOLD.toString() + Formatting.GRAY, boldGreen = Formatting.BOLD.toString() + Formatting.GREEN;

        // Delay Packets
        addDrawableChild(ButtonWidget.builder(Text.of("Delay packets: " + Variables.delayUIPackets), button -> {
            Variables.delayUIPackets = !Variables.delayUIPackets;

            button.setMessage(Text.of("Delay packets: " + Variables.delayUIPackets));

            if (!Variables.delayUIPackets && !Variables.delayedPackets.isEmpty()) {
                for (final Packet<?> packet : Variables.delayedPackets) {
                    mc.player.networkHandler.sendPacket(packet);
                }
                int delayedPacketsCount = Variables.delayedPackets.size();
                mc.player.sendMessage(Text.of(boldGray + "Successfully sent " + boldGreen + delayedPacketsCount + Formatting.GRAY + " delayed packets."));
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
            mc.player.sendMessage(Text.literal("Screen§a successfully§r saved! Press §a" + restoreScreenBind.getString() + " §rto restore it!"));
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
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        assert client != null;

        // Sync ID and Revision
        Benefit.renderTexts(context, client.textRenderer, client);
    }
}