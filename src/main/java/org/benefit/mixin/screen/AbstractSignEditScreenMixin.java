package org.benefit.mixin.screen;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.benefit.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen {
    protected AbstractSignEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow @Final private SignBlockEntity blockEntity;

    @Inject(at = @At("TAIL"), method = "init")
    private void init(CallbackInfo ci) {
        if(Benefit.config.getLayoutMode() == LayoutMode.NONE) return;
        drawButtons();
    }

    @Unique
    private void drawButtons() {
        if(client == null || client.player == null || client.player.networkHandler == null) return;
        final int x = LayoutPos.xValue(80);
        // Delay Packets
        addDrawableChild(ButtonWidget.builder(Text.literal("Delay packets: " + Variables.delayUIPackets), button -> {
            Variables.delayUIPackets = !Variables.delayUIPackets;
            button.setMessage(Text.literal("Delay packets: " + Variables.delayUIPackets));
            if (!Variables.delayUIPackets && !Variables.delayedPackets.isEmpty()) {
                for(final Packet<?> packet : Variables.delayedPackets) {
                    client.player.networkHandler.sendPacket(packet);
                }
                final String msg = String.format("§7Successfully sent §a%s §7delayed packets.", Variables.delayedPackets.size());
                client.player.sendMessage(Text.literal(msg));
                Variables.delayedPackets.clear();
            }
        }).width(120).position(LayoutPos.xValue(120), LayoutPos.signBaseY()).build());

        // Soft Close
        addDrawableChild(ButtonWidget.builder(Text.literal("Soft Close"), button -> client.setScreen(null))
                .width(80).position(x, LayoutPos.signBaseY() - 50).build());

        // Save UI
        addDrawableChild(ButtonWidget.builder(Text.literal("Save UI"), button -> {
            Variables.storedScreen = client.currentScreen;
            Variables.storedScreenHandler = client.player.currentScreenHandler;
            client.setScreen(null);
            client.player.sendMessage(Text.literal("Screen§a successfully§r saved! Press §a" + Benefit.restoreScreenBind.getString() + " §rto restore it!"));
        }).tooltip(Tooltip.of(Text.literal("Delay packets has to be enabled in order to Save UI without updating the sign.")))
          .width(80)
          .position(x, LayoutPos.signBaseY() - 26)
          .build());
    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(Benefit.config.getLayoutMode() == LayoutMode.NONE) return;
        drawText(context);
    }

    @Unique
    private void drawText(DrawContext context) {
        // We can actually get the position of the sign we're in which is neat.
        final BlockPos pos = blockEntity.getPos() != null ? blockEntity.getPos() : BlockPos.ORIGIN;
        final Text signPos = Text.literal("Sign Pos: " + (pos.equals(BlockPos.ORIGIN) ? "INVALID" : pos.toShortString()));
        final int width = textRenderer.getWidth(signPos);
        context.drawText(textRenderer, signPos,
                LayoutPos.xValue(width), LayoutPos.signPosY(),
                -1, false);
    }

}