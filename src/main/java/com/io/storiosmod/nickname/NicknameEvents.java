package com.io.storiosmod.nickname;

import com.io.storiosmod.StoriosMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
