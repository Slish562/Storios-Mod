package com.io.storiosmod.events;

import com.io.storiosmod.registries.AttributesRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "storios_mod", bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerAttributeSetup {

    @SubscribeEvent
    public static void addPlayerAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AttributesRegistry.FLAT_DAMAGE_REDUCTION.get());
    }
}