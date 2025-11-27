package com.io.storiosmod.events;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.registries.AttributesRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.EventPriority;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FlatDamageHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        var attr = player.getAttribute(AttributesRegistry.FLAT_DAMAGE_REDUCTION.get());
        if (attr == null) return;

        double reduction = attr.getValue();
        if (reduction <= 0.0D) return;

        float damage = event.getAmount();
        float finalDamage = Math.max(0.0f, damage - (float) reduction);

        event.setAmount(finalDamage);
    }
}