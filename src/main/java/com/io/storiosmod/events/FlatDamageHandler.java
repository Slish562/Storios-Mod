package com.io.storiosmod.events;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.registries.AttributesRegistry;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.EventPriority;

@EventBusSubscriber(modid = StoriosMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class FlatDamageHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;

        var attr = player.getAttribute(AttributesRegistry.FLAT_DAMAGE_REDUCTION);
        if (attr == null) return;

        double reduction = attr.getValue();
        if (reduction <= 0.0D) return;

        float damage = event.getNewDamage();
        float finalDamage = Math.max(0.0f, damage - (float) reduction);

        event.setNewDamage(finalDamage);
    }
}

