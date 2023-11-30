package org.benefit.mixin;

import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.apache.commons.lang3.ArrayUtils;
import org.benefit.Client;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VideoOptionsScreen.class, priority = 1337) @Debug(export = true)
public abstract class VideoOptionsScreenMixin {
    @Inject(at = @At("RETURN"), method = "getOptions", cancellable = true)
    private static void getOptions(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        SimpleOption<?>[] values = cir.getReturnValue();
        cir.setReturnValue(ArrayUtils.insert(9, values, Client.getFormat()));
    }

}
