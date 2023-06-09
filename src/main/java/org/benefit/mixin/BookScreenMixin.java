package org.benefit.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.benefit.Client;
import org.benefit.Variables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;



@Mixin(BookScreen.class)
public abstract class BookScreenMixin extends Screen {
    @Shadow
    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);
    protected BookScreenMixin(Text title) {
        super(title);
    }
//define variables
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final ClientPlayerEntity mcp = mc.player;

//the main method
    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
    //check if screen is a lecturn or book
        if (client.currentScreen instanceof LecternScreen || client.currentScreen instanceof BookScreen || client.currentScreen instanceof BookEditScreen) {
        //simplify expressions
            Boolean sendUIPackets = Variables.sendUIPackets;
            ScreenHandler CurrentScreenHandler = mcp.currentScreenHandler;
            Screen CurrentScreen = mc.currentScreen;
            ClientPlayNetworkHandler mcNetworkHandler = mc.getNetworkHandler();

        //define variables
            int syncID = mcp.currentScreenHandler.syncId;
            CloseHandledScreenC2SPacket CloseHandledScreen = new CloseHandledScreenC2SPacket(syncID);

        //define color codes
            String bold = "§l";
            String lGray = "§7";
            String green = "§a";

        //combine bold and lightgray
            String bGray = bold + lGray;

        //combine bold and green
            String bGreen = bold + green;

        //define button names
            Text softClose = Text.of("Soft Close");
            Text deSync = Text.of("De-sync");
            Text saveUI = Text.of("Save UI");
            Text leaveNSend = Text.of("Leave & send packets");


        //add in send packets button
            addDrawableChild(ButtonWidget.builder(Text.of("Send Packets: " + sendUIPackets), button -> {
                Variables.sendUIPackets = !Variables.sendUIPackets;

            //setting the text on the button to true or false when it is active
                button.setMessage(Text.of("Send Packets: " + Variables.sendUIPackets));

            }).dimensions(4, 65, 120, 20).build());

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
                    mc.player.sendMessage(Text.of(bGray + "Successfully sent " + bGreen + DelayedPacketsCount + lGray + " delayed packets."));
                    Variables.delayedPackets.clear();
                }
            }).width(120).position(4, 35).build());

        //add in softclose button
            addDrawableChild(ButtonWidget.builder(softClose, (button) -> mc.setScreen(null)).width(80).position(4, 5).build());

        //add in desync button
            addDrawableChild(ButtonWidget.builder(deSync, (button) -> mcNetworkHandler.sendPacket(CloseHandledScreen)).width(80).position(4, 155).build());

        //add in save ui button
            addDrawableChild(ButtonWidget.builder(saveUI, (button) -> {

            //define variables
                Variables.storedScreen = CurrentScreen;
                Variables.storedScreenHandler = CurrentScreenHandler;

            }).width(80).position(4, 95).build());

        //add in leave n send packets button
            addDrawableChild(ButtonWidget.builder(leaveNSend, (button) -> {

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
            }).width(140).position(4, 125).build());
        }
    }
    //make sinkid and revision visible
        @Inject(at = @At("TAIL"), method = "render")
        public void render (MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci){
            Client.renderHandledScreen(mc, textRenderer, matrices);
        }
}