package com.io.storiosmod.sidebar;

import com.io.storiosmod.StoriosMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SidebarEvents {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
