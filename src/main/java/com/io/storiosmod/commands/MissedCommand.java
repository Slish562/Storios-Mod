package com.io.storiosmod.commands;

import com.io.storiosmod.chat.ChatHistoryManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class MissedCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("missed")
                .executes(MissedCommand::showMissed));
    }

    private static int showMissed(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            List<ChatHistoryManager.ChatEntry> missed = ChatHistoryManager.getMissedMessages(player.getUUID());

            if (missed.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal("\u00A77No missed messages"), false);
                return 0;
            }

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(ChatHistoryManager.formatMissedMessages(missed));
            player.sendSystemMessage(Component.literal(""));

            ChatHistoryManager.clearLogoutTime(player.getUUID());
            return missed.size();
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("\u00A7cError: " + e.getMessage()));
            return 0;
        }
    }
}


