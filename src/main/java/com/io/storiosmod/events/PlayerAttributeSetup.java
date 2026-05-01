package com.io.storiosmod.events;

import com.io.storiosmod.registries.AttributesRegistry;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "storios_mod", bus = EventBusSubscriber.Bus.MOD)
public class PlayerAttributeSetup {

    @SubscribeEvent
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AttributesRegistry.FLAT_DAMAGE_REDUCTION);
    }
}

