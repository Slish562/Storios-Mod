package com.io.storiosmod.commands;

import com.io.storiosmod.tablist.TabListManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Locale;

public class TabStyleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tabstyle")
                .then(Commands.literal("list")
                        .executes(TabStyleCommand::listStyles))
                .then(Commands.literal("switch")
                        .then(Commands.argument("style", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                                    for (String id : TabListManager.getStyleIds()) {
                                        if (id.toLowerCase(Locale.ROOT).contains(remaining)) {
                                            builder.suggest(id);
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(TabStyleCommand::switchStyle)))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(TabStyleCommand::reload)));
    }

    private static int listStyles(CommandContext<CommandSourceStack> ctx) {
        var ids = TabListManager.getStyleIds();
        if (ids.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("\u00A7cNo tab styles loaded"));
            return 0;
        }

        ctx.getSource().sendSuccess(() -> Component.literal("\u00A76\u00A7lAvailable Tab Styles:"), false);
        for (String id : ids) {
            String name = TabListManager.getStyleName(id);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("  \u00A7e" + id + " \u00A77\u2014 \u00A7f" + name), false);
        }
        return ids.size();
    }

    private static int switchStyle(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String styleId = StringArgumentType.getString(ctx, "style");

            if (!TabListManager.styleExists(styleId)) {
                ctx.getSource().sendFailure(Component.literal("\u00A7cStyle '" + styleId + "' not found! Use /tabstyle list"));
                return 0;
            }

            TabListManager.setPlayerStyle(player, styleId);
            String styleName = TabListManager.getStyleName(styleId);
            ctx.getSource().sendSuccess(() ->
                    Component.literal("\u00A7aTab style changed to: \u00A7e" + styleName), false);
            return 1;
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.literal("\u00A7cOnly players can use this command"));
            return 0;
        }
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        if (TabListManager.reload()) {
            ctx.getSource().sendSuccess(() -> Component.literal("\u00A7aTab styles reloaded"), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(Component.literal("\u00A7cFailed to reload tab styles"));
            return 0;
        }
    }
}


