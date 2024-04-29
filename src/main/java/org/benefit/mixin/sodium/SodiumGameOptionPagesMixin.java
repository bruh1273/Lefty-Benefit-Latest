package org.benefit.mixin.sodium;

import com.google.common.collect.ImmutableList;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.minecraft.text.Text;
import org.benefit.Benefit;
import org.benefit.LayoutMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
public abstract class SodiumGameOptionPagesMixin {

    @Shadow @Final private static MinecraftOptionsStorage vanillaOpts;

    @Inject(at = @At("TAIL"), method = "advanced", cancellable = true)
    private static void addBenefitLayout(CallbackInfoReturnable<OptionPage> cir) {
        List<OptionGroup> groups = new ArrayList<>(cir.getReturnValue().getGroups());
        List<Option<?>> options = new ArrayList<>(cir.getReturnValue().getOptions());

        options.add(2, OptionImpl.createBuilder(Boolean.TYPE, vanillaOpts)
                .setName(Text.translatable("benefit.json"))
                .setTooltip(Text.translatable("benefit.json.tooltip"))
                .setBinding((option, value) -> Benefit.copyJson.setValue(value), option -> Benefit.copyJson.getValue())
                .setControl(TickBoxControl::new)
                .build());

        options.add(2, OptionImpl.createBuilder(LayoutMode.class, vanillaOpts)
                .setName(Text.translatable("benefit.format"))
                .setTooltip(Text.translatable("benefit.format.tooltip"))
                .setBinding((option, value) -> Benefit.format.setValue(value), option -> Benefit.format.getValue())
                .setControl(control -> new CyclingControl<>(control, LayoutMode.class, LayoutMode.translationKeys))
                .build());

        options.add(2, OptionImpl.createBuilder(Boolean.TYPE, vanillaOpts)
                .setName(Text.translatable("benefit.overlay"))
                .setTooltip(Text.translatable("benefit.overlay.tooltip"))
                .setBinding((option, value) -> Benefit.overlay.setValue(value), option -> Benefit.overlay.getValue())
                .setControl(TickBoxControl::new)
                .build());

        OptionGroup.Builder builder = OptionGroup.createBuilder();
        options.forEach(builder::add);
        groups.set(1, builder.build());

        cir.setReturnValue(new OptionPage(Text.translatable("sodium.options.pages.advanced"), ImmutableList.copyOf(groups)));
    }
}
