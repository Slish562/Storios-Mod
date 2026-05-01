package com.io.storiosmod.registries;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.attribute.FlatDamageReductionAttribute;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class AttributesRegistry {

    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, StoriosMod.MODID);

    public static final DeferredHolder<Attribute, Attribute> FLAT_DAMAGE_REDUCTION = ATTRIBUTES.register(
            "flat_damage_reduction",
            FlatDamageReductionAttribute::new
    );
    public static void register(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
    }
}
