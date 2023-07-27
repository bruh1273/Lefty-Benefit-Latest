package org.benefit.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.benefit.Variables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.benefit.Client.mc;


@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {
    protected BookScreenMixin(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        //simplify expressions
        String bGray = Formatting.BOLD.toString() + Formatting.GRAY;
        String bGreen = Formatting.BOLD.toString() + Formatting.GREEN;

        //display sync id
        Text syncId = Text.of("Sync Id: " + mc.player.currentScreenHandler.syncId);
        addDrawableChild(new TextWidget(4, 5, textRenderer.getWidth(syncId) + 4, 20, syncId, textRenderer));

        //display revision
        Text revision = Text.of("Revision: " + mc.player.currentScreenHandler.getRevision());
        addDrawableChild(new TextWidget(4, 25, textRenderer.getWidth(revision) + 4, 20, revision, textRenderer));

        //add in send packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Send Packets: " + Variables.sendUIPackets), button -> {
            Variables.sendUIPackets = !Variables.sendUIPackets;

            //setting the text on the button to true or false when it is active
            button.setMessage(Text.of("Send Packets: " + Variables.sendUIPackets));
        }).dimensions(4, 105, 120, 20).build());

        //add in delay packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Delay packets: " + Variables.delayUIPackets), (button) -> {
            Variables.delayUIPackets = !Variables.delayUIPackets;

            //setting the text on the button to true or false when it is active
            button.setMessage(Text.of("Delay packets: " + Variables.delayUIPackets));

            //condition to see if any delayed packets was delayed, then send them
            if (!Variables.delayUIPackets && !Variables.delayedPackets.isEmpty()) {
                for (Packet<?> packet : Variables.delayedPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }

                //add in message to say how many delayed packets were sent
                int DelayedPacketsCount = Variables.delayedPackets.size();
                mc.player.sendMessage(Text.of(bGray + "Successfully sent " + bGreen + DelayedPacketsCount + Formatting.GRAY + " delayed packets."));
                Variables.delayedPackets.clear();
            }
        }).width(120).position(4, 75).build());

        //add in softclose button
        addDrawableChild(ButtonWidget.builder(Text.of("Soft Close"), (button) -> mc.setScreen(null)).width(80).position(4, 45).build());

        //add in desync button
        addDrawableChild(ButtonWidget.builder(Text.of("De-sync"), (button) -> {
            int syncID = mc.player.currentScreenHandler.syncId;
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(syncID));
        }).width(80).position(4, 195).build());

        //add in save ui button
        addDrawableChild(ButtonWidget.builder(Text.of("Save UI"), (button) -> {
            //define variables
            Variables.storedScreen = mc.currentScreen;
            Variables.storedScreenHandler = mc.player.currentScreenHandler;
        }).width(80).position(4, 135).build());

        //add in leave n send packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Leave & send packets"), (button) -> {

            if (!Variables.delayedPackets.isEmpty()) {
                Variables.delayUIPackets = false;

                for (Packet<?> packet : Variables.delayedPackets) {
                    mc.getNetworkHandler().sendPacket(packet);
                }
                //add in message to say how many delayed packets were sent
                int DelayedPacketsAmount = Variables.delayedPackets.size();

                //disconnect player
                mc.getNetworkHandler().getConnection().disconnect(Text.of(bGray + "Disconnected, " + bGreen + DelayedPacketsAmount + bGray + " packets successfully sent."));
                Variables.delayedPackets.clear();
            }
        }).width(140).position(4, 165).build());
    }
}