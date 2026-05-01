package com.io.storiosmod.sidebar;

import com.io.storiosmod.StoriosMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = StoriosMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class SidebarEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {

        if (event.getServer() == null) return;

        SidebarManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String style = SidebarManager.getPlayerStyle(player);
        if (!"off".equals(style)) {
            SidebarManager.sendImmediate(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        SidebarManager.cleanupPlayer(player.getUUID());
    }
}

