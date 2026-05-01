package com.io.storiosmod.nickname;

import com.io.storiosmod.StoriosMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = StoriosMod.MODID, bus = EventBusSubscriber.Bus.GAME)
public class NicknameEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Component displayName = NicknameManager.getCachedDisplayName(player);
        if (displayName != null) {
            event.setDisplayname(displayName);
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (NicknameManager.hasNickname(player)) {
            player.refreshDisplayName();
        }
    }

    @SubscribeEvent
    public static void onTabListName(PlayerEvent.TabListNameFormat event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Component displayName = NicknameManager.getCachedDisplayName(player);
        if (displayName != null) {
            event.setDisplayName(displayName);
        }
    }
}

