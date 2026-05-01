package com.io.storiosmod.chat;

import com.io.storiosmod.StoriosMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = StoriosMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChatHistoryEvents {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onChatFormat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String rawText = event.getRawText();

        ChatStyleConfig config = ChatStyleConfig.get();
        String format = config.chatFormat;
        if (format == null || format.isEmpty()) {
            format = "<{player}> {message}";
        }

        Component styledMessage;
        ChatColorManager.ChatColorData colorData = ChatColorManager.getChatColor(player);
        if (colorData != null) {
            styledMessage = ChatColorManager.colorize(rawText, colorData);
        } else {
            styledMessage = ChatFormatter.format(rawText);
        }

        Component playerName = player.getDisplayName();
        UUID playerUuid = player.getUUID();
        MutableComponent finalMessage = buildChatLine(format, playerName, styledMessage, playerUuid);

        event.setCanceled(true);
        player.getServer().getPlayerList().broadcastSystemMessage(finalMessage, false);

        ChatHistoryManager.recordMessage(
                player.getGameProfile().getName(), rawText, System.currentTimeMillis());
    }

    private static MutableComponent buildChatLine(String format, Component playerName, Component message,
            UUID playerUuid) {
        MutableComponent root = Component.literal("");
        String remaining = format;

        while (!remaining.isEmpty()) {
            int playerIdx = remaining.indexOf("{player}");
            int messageIdx = remaining.indexOf("{message}");
            int headIdx = remaining.indexOf("{head}");

            int nextIdx = -1;
            String nextToken = null;

            int[] candidates = { playerIdx, messageIdx, headIdx };
            String[] tokens = { "{player}", "{message}", "{head}" };
            for (int c = 0; c < candidates.length; c++) {
                if (candidates[c] >= 0 && (nextIdx < 0 || candidates[c] < nextIdx)) {
                    nextIdx = candidates[c];
                    nextToken = tokens[c];
                }
            }

            if (nextIdx < 0) {
                root.append(ChatFormatter.format(remaining));
                break;
            }

            if (nextIdx > 0) {
                root.append(ChatFormatter.format(remaining.substring(0, nextIdx)));
            }

            if ("{player}".equals(nextToken)) {
                root.append(playerName);
            } else if ("{message}".equals(nextToken)) {
                root.append(message);
            } else if ("{head}".equals(nextToken)) {
                root.append(ChatHeadMarker.create(playerUuid));
            }

            remaining = remaining.substring(nextIdx + nextToken.length());
        }

        return root;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ChatHistoryManager.recordLogout(player.getUUID());
        }
    }
}
