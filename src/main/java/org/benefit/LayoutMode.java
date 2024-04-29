package org.benefit;

import net.minecraft.text.Text;
import net.minecraft.util.TranslatableOption;
import net.minecraft.util.function.ValueLists;

import java.util.function.IntFunction;

public enum LayoutMode implements TranslatableOption {
    TOP_LEFT(0, "benefit.format.default"),
    TOP_RIGHT(1, "benefit.format.topright"),
    BOTTOM_LEFT(2, "benefit.format.bottomleft"),
    BOTTOM_RIGHT(3, "benefit.format.bottomright"),
    NONE(4, "benefit.format.disabled");

    private static final IntFunction<LayoutMode> BY_ID = ValueLists.createIdToValueFunction(LayoutMode::getId, values(), ValueLists.OutOfBoundsHandling.WRAP);
    private final int id;
    private final String translationKey;

    LayoutMode(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public String toString() {
        return switch(this) {
            case TOP_LEFT -> "Default";
            case TOP_RIGHT -> "Top Right";
            case BOTTOM_LEFT -> "Bottom Left";
            case BOTTOM_RIGHT -> "Bottom Right";
            case NONE -> "Disabled";
        };
    }

    public static final Text[] translationKeys = {
            Text.translatable(TOP_LEFT.getTranslationKey()),
            Text.translatable(TOP_RIGHT.getTranslationKey()),
            Text.translatable(BOTTOM_LEFT.getTranslationKey()),
            Text.translatable(BOTTOM_RIGHT.getTranslationKey()),
            Text.translatable(NONE.getTranslationKey())
    };

    public static LayoutMode byId(int id) {
        return BY_ID.apply(id);
    }


}