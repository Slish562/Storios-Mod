package com.io.storiosmod.commands;

import com.io.storiosmod.chat.ChatStyleConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ChatStyleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chatstyle")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload")
                        .executes(ChatStyleCommand::reload)));
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        if (ChatStyleConfig.reload()) {
            ctx.getSource().sendSuccess(() -> Component.literal("\u00A7aChat style config reloaded"), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("\u00A7cFailed to reload chat style config"));
            return 0;
        }
    }
}


