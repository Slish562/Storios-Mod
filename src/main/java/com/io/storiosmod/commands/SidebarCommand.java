package com.io.storiosmod.commands;

import com.io.storiosmod.sidebar.SidebarManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public class SidebarCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sidebar")
                .then(Commands.literal("list")
                        .executes(SidebarCommand::listStyles))
                .then(Commands.literal("switch")
                        .then(Commands.argument("style", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                                    for (String id : SidebarManager.getStyleIds()) {
                                        if (id.toLowerCase(Locale.ROOT).contains(remaining)) {
                                            builder.suggest(id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(SidebarCommand::switchStyle)))
                .then(Commands.literal("off")
                        .executes(SidebarCommand::turnOff))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(SidebarCommand::reload)));
    }

    private static int listStyles(CommandContext<CommandSourceStack> ctx) {
        var ids = SidebarManager.getStyleIds();
        if (ids.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("§cNo sidebar styles loaded"));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§6§lAvailable Sidebar Styles:"), false);
        for (String id : ids) {
            String name = SidebarManager.getStyleName(id);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("  §e" + id + " §7— §f" + name), false);
        }
        return ids.size();
    }

    private static int switchStyle(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String styleId = StringArgumentType.getString(ctx, "style");

            if (!SidebarManager.styleExists(styleId)) {
                ctx.getSource().sendFailure(Component.literal("§cStyle '" + styleId + "' not found! Use /sidebar list"));
                return 0;
            }

            SidebarManager.setPlayerStyle(player, styleId);
            String styleName = SidebarManager.getStyleName(styleId);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("§aSidebar changed to: §e" + styleName), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cOnly players can use this command"));
            return 0;
        }
    }

    private static int turnOff(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            SidebarManager.setPlayerStyle(player, "off");
            ctx.getSource().sendSuccess(() -> Component.literal("§7Sidebar hidden"), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("§cOnly players can use this command"));
            return 0;
        }
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        if (SidebarManager.reload()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§aSidebar styles reloaded"), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("§cFailed to reload sidebar styles"));
            return 0;
        }
    }
}
