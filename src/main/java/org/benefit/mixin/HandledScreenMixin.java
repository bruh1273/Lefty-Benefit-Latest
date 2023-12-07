package org.benefit.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.benefit.Client;
import org.benefit.LayoutMode;
import org.benefit.LayoutPos;
import org.benefit.Variables;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.benefit.Client.mc;
import static org.benefit.Client.restoreScreenBind;


@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin extends Screen {
    @Shadow protected int x;

    @Shadow protected int y;

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

        //add in send packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Send Packets: " + Variables.sendUIPackets), button -> {
            Variables.sendUIPackets = !Variables.sendUIPackets;

            //setting the text on the button to true or false when it is active
            button.setMessage(Text.of("Send Packets: " + Variables.sendUIPackets));
        }).dimensions(LayoutPos.xValue(120), LayoutPos.baseY() - 90, 120, 20).build());

        //add in delay packets button
        addDrawableChild(ButtonWidget.builder(Text.of("Delay packets: " + Variables.delayUIPackets), (button) -> {
            Variables.delayUIPackets = !Variables.delayUIPackets;

            //setting the text on the button to true or false when it is active
            button.setMessage(Text.of("Delay packets: " + Variables.delayUIPackets));

            //condition to see if any delayed packets was delayed, then send them
            if (!Variables.delayUIPackets && !Variables.delayedPackets.isEmpty()) {
                for (Packet<?> packet : Variables.delayedPackets)
                    mc.getNetworkHandler().sendPacket(packet);


                //add in message to say how many delayed packets were sent
                int DelayedPacketsCount = Variables.delayedPackets.size();
                mc.player.sendMessage(Text.of(bGray + "Successfully sent " + bGreen + DelayedPacketsCount + Formatting.GRAY + " delayed packets."));
                Variables.delayedPackets.clear();
            }
        }).width(120).position(LayoutPos.xValue(120), LayoutPos.baseY() - 120).build());

        //add in softclose button
        addDrawableChild(ButtonWidget.builder(Text.of("Soft Close"), (button) -> mc.setScreen(null))
                .width(80).position(LayoutPos.xValue(80), LayoutPos.baseY() - 150).build());

        //add in desync button
        addDrawableChild(ButtonWidget.builder(Text.of("De-sync"), (button) -> {
            int syncID = mc.player.currentScreenHandler.syncId;
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(syncID));
        }).width(80).position(LayoutPos.xValue(80), LayoutPos.baseY()).build());

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
                    mc.getNetworkHandler().sendPacket(packet);
                }
                //add in message to say how many delayed packets were sent
                int DelayedPacketsAmount = Variables.delayedPackets.size();

                //disconnect player
                mc.getNetworkHandler().getConnection().disconnect(
                        Text.of(bGray + "Disconnected, " + bGreen + DelayedPacketsAmount + bGray + " packets successfully sent."));
                Variables.delayedPackets.clear();
            }
        }).width(140).position(LayoutPos.xValue(140), LayoutPos.baseY() - 30).build());
    }

    @Inject(at = @At("RETURN"), method = "render")
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        // Add in slot overlay
        if(Client.config.getLayoutMode() != LayoutMode.NONE && Client.config.getOverlayValue()) Client.addText(context, client.textRenderer, client, this.x, this.y);

        // Add in Sync ID and Revision on screen.
        Client.renderTexts(context, client.textRenderer, client);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Release any alt key and the color of the slot overlay will go away.
        final int clr = 0xFF828282;
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT && Client.txtColor != clr)
            Client.txtColor = clr;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }
    @Inject(at = @At("HEAD"), method = "keyPressed")
    public void keyPres(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if(keyCode == GLFW.GLFW_KEY_LEFT_ALT || keyCode == GLFW.GLFW_KEY_RIGHT_ALT && Client.txtColor != -1)
            Client.txtColor = -1;
    }
}