package org.benefit.mixin.sodium;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.binding.GenericBinding;
import me.jellysquid.mods.sodium.client.gui.options.binding.OptionBinding;
import me.jellysquid.mods.sodium.client.gui.options.binding.compat.VanillaBooleanOptionBinding;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.text.Text;
import org.benefit.Client;
import org.benefit.Config;
import org.benefit.LayoutMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;


@Mixin(SodiumGameOptionPages.class)
public abstract class SodiumGameOptionPagesMixin {
    @Shadow @Final private static MinecraftOptionsStorage vanillaOpts;
    @Inject(at = @At("TAIL"), method = "advanced", remap = false, cancellable = true)
    private static void addBenefitLayout(CallbackInfoReturnable<OptionPage> cir) {
        List<OptionGroup> groups = new ArrayList<>(cir.getReturnValue().getGroups());
        List<Option<?>> options = new ArrayList<>(cir.getReturnValue().getOptions());
        options.add(2, OptionImpl.createBuilder(LayoutMode.class, vanillaOpts)
                .setName(Text.translatable("benefit.format"))
                .setControl(ctrl -> {
                    return new CyclingControl<>(ctrl, LayoutMode.class, new Text[] {
                            Text.translatable("benefit.format.default"),
                            Text.translatable("benefit.format.topright"),
                            Text.translatable("benefit.format.bottomleft"),
                            Text.translatable("benefit.format.bottomright"),
                            Text.translatable("benefit.format.disabled")
                    });
                }).setTooltip(Text.translatable("benefit.format.tooltip"))
                .setBinding((opt, val) -> Client.format.setValue(val), opt -> {
                    return Client.format.getValue();
                }).build());
        options.add(2, OptionImpl.createBuilder(Boolean.TYPE, vanillaOpts)
                .setName(Text.translatable("benefit.overlay")).setTooltip(Text.translatable("benefit.overlay.tooltip"))
                .setBinding((option, value) -> Client.overlay.setValue(value), option -> Client.overlay.getValue())
                .setControl(TickBoxControl::new).build());
        OptionGroup.Builder builder = OptionGroup.createBuilder();
        options.forEach(builder::add);
        groups.set(1, builder.build());
        cir.setReturnValue(new OptionPage(Text.translatable("sodium.options.pages.advanced"), ImmutableList.copyOf(groups)));
    }
}
