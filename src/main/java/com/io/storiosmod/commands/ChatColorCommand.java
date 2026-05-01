package com.io.storiosmod.commands;

import com.io.storiosmod.chat.ChatColorManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ChatColorCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cmessages")
                .then(Commands.literal("enable")
                        .then(Commands.literal("solid")
                                .then(Commands.argument("color", StringArgumentType.string())
                                        .executes(ChatColorCommand::enableSolid)))
                        .then(Commands.literal("gradient")
                                .then(Commands.argument("startColor", StringArgumentType.string())
                                        .then(Commands.argument("endColor", StringArgumentType.string())
                                                .executes(ChatColorCommand::enableGradient)))))
                .then(Commands.literal("disable")
                        .executes(ChatColorCommand::disable)));
    }

    private static int enableSolid(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String colorStr = StringArgumentType.getString(ctx, "color");

            validateColor(colorStr);
            ChatColorManager.ChatColorData data = ChatColorManager.ChatColorData.solid(colorStr);
            ChatColorManager.setChatColor(player, data);

            Component preview = ChatColorManager.colorize("This is how your messages will look!", data);
            ctx.getSource().sendSuccess(() -> Component.literal("§aChat color enabled! Preview: ").append(preview),
                    false);
            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("§cInvalid color format! Use hex codes like #FF0000"));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int enableGradient(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String startColorStr = StringArgumentType.getString(ctx, "startColor");
            String endColorStr = StringArgumentType.getString(ctx, "endColor");

            validateColor(startColorStr);
            validateColor(endColorStr);
            ChatColorManager.ChatColorData data = ChatColorManager.ChatColorData.gradient(startColorStr, endColorStr);
            ChatColorManager.setChatColor(player, data);

            Component preview = ChatColorManager.colorize("This is how your messages will look!", data);
            ctx.getSource().sendSuccess(() -> Component.literal("§aGradient chat enabled! Preview: ").append(preview),
                    false);
            return 1;
        } catch (IllegalArgumentException e) {
            ctx.getSource().sendFailure(Component.literal("§cInvalid color format! Use hex codes like #FF0000"));
            return 0;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static int disable(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ChatColorManager.removeChatColor(player);
            ctx.getSource().sendSuccess(() -> Component.literal("§7Chat color disabled"), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
            return 0;
        }
    }

    private static void validateColor(String colorStr) {
        String hex = colorStr.startsWith("#") ? colorStr.substring(1) : colorStr;
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Color must be 6 hex characters");
        }
        Integer.parseInt(hex, 16);
    }
}
