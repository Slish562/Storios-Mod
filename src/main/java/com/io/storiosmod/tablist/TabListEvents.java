package com.io.storiosmod.tablist;

import com.io.storiosmod.StoriosMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TabListEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        TabListManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        TabListManager.sendImmediate(player);
    }
}
