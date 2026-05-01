package com.io.storiosmod.chat;

import com.io.storiosmod.StoriosMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatStyleEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ChatStyleConfig config = ChatStyleConfig.get();
        String format = config.joinFormat;
        if (format == null || format.isEmpty()) return;

        String message = format.replace("{player}", player.getGameProfile().getName());
        Component component = ChatFormatter.format(message);

        player.getServer().getPlayerList().broadcastSystemMessage(component, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ChatStyleConfig config = ChatStyleConfig.get();
        String format = config.leaveFormat;
        if (format == null || format.isEmpty()) return;

        String message = format.replace("{player}", player.getGameProfile().getName());
        Component component = ChatFormatter.format(message);

        player.getServer().getPlayerList().broadcastSystemMessage(component, false);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ChatStyleConfig config = ChatStyleConfig.get();
        String format = config.deathFormat;
        if (format == null || format.isEmpty() || format.equals("{death_message}")) return;

        Component vanillaDeath = player.getCombatTracker().getDeathMessage();
        String message = format
                .replace("{player}", player.getGameProfile().getName())
                .replace("{death_message}", vanillaDeath.getString());
        Component component = ChatFormatter.format(message);

        player.getServer().getPlayerList().broadcastSystemMessage(component, false);
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        Advancement advancement = event.getAdvancement();
        if (advancement.getDisplay() == null) return;

        ChatStyleConfig config = ChatStyleConfig.get();
        String format = config.advancementFormat;
        if (format == null || format.isEmpty()) return;

        String advancementTitle = advancement.getDisplay().getTitle().getString();
        String message = format
                .replace("{player}", player.getGameProfile().getName())
                .replace("{advancement}", advancementTitle);
        Component component = ChatFormatter.format(message);

        player.getServer().getPlayerList().broadcastSystemMessage(component, false);
    }
}
