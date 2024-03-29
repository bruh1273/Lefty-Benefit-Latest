package org.benefit.mixin;

import net.minecraft.network.*;
import net.minecraft.network.packet.*;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.play.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import org.benefit.Variables;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
    public void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        // The method to store the delayed packets
        if (Variables.delayUIPackets && (packet instanceof ClickSlotC2SPacket || packet instanceof ButtonClickC2SPacket || packet instanceof CloseHandledScreenC2SPacket)) {
            Variables.delayedPackets.add(packet);
            ci.cancel();
        }

        // Soft close for signs handling
        if (!Variables.shouldEditSign && (packet instanceof UpdateSignC2SPacket)) {
            Variables.shouldEditSign = true;
            ci.cancel();
        }
    }
}
