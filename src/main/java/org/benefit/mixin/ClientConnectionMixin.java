package org.benefit.mixin;

import net.minecraft.network.*;
import net.minecraft.network.packet.*;
import net.minecraft.network.packet.c2s.play.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import org.benefit.Variables;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
    private void sendImmediately(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        // Store the delayed packets
        if (Variables.delayUIPackets && (packet instanceof ClickSlotC2SPacket
                        || packet instanceof ButtonClickC2SPacket
                        || packet instanceof CloseHandledScreenC2SPacket
                        || packet instanceof UpdateSignC2SPacket
                        || packet instanceof RenameItemC2SPacket)) {
            Variables.delayedPackets.add(packet);
            ci.cancel();
        }
    }
}