package com.io.storiosmod.attribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class FlatDamageReductionAttribute extends RangedAttribute {

    public FlatDamageReductionAttribute() {
        super("attribute.storios_mod.flat_damage_reduction", 0.0D, 0.0D, 100000.0D);
        this.setSyncable(true);
    }

    @Override
    public double sanitizeValue(double value) {
        return Math.max(0.0D, Math.min(this.getMaxValue(), value));
    }
}
