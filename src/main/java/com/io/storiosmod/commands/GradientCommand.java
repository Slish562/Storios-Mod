package com.io.storiosmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class GradientCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gtellraw")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("gradient")
                                .then(Commands.argument("startColor", StringArgumentType.string())
                                        .then(Commands.argument("endColor", StringArgumentType.string())
                                                .then(Commands.argument("message", StringArgumentType.greedyString())
                                                        .executes(GradientCommand::executeGradient)))))
                        .then(Commands.literal("solid")
                                .then(Commands.argument("color", StringArgumentType.string())
                                        .then(Commands.argument("message", StringArgumentType.greedyString())
                                                .executes(GradientCommand::executeSolid))))));
    }

    private static int executeGradient(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String startColorStr = StringArgumentType.getString(context, "startColor");
            String endColorStr = StringArgumentType.getString(context, "endColor");
            String message = StringArgumentType.getString(context, "message");

            int startColor = parseColor(startColorStr);
            int endColor = parseColor(endColorStr);

            Component gradientMessage = createGradient(message, startColor, endColor);

            for (ServerPlayer player : targets) {
                player.sendSystemMessage(gradientMessage);
            }

            return targets.size();
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid color format. Use hex codes (e.g., #FF0000)."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeSolid(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String colorStr = StringArgumentType.getString(context, "color");
            String message = StringArgumentType.getString(context, "message");

            int color = parseColor(colorStr);

            Component solidMessage = createSolid(message, color);

            for (ServerPlayer player : targets) {
                player.sendSystemMessage(solidMessage);
            }

            return targets.size();
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid color format. Use hex codes (e.g., #FF0000)."));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }

    private static Component createGradient(String text, int startColor, int endColor) {
        MutableComponent root = Component.literal("");
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (length > 1) ? (float) i / (float) (length - 1) : 0;
            int interpolatedColor = interpolateColor(startColor, endColor, ratio);

            Style style = Style.EMPTY.withColor(TextColor.fromRgb(interpolatedColor));
            root.append(Component.literal(String.valueOf(text.charAt(i))).setStyle(style));
        }

        return root;
    }

    private static Component createSolid(String text, int color) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(color));
        return Component.literal(text).setStyle(style);
    }

    private static int parseColor(String colorStr) {
        if (colorStr.startsWith("#")) {
            return Integer.parseInt(colorStr.substring(1), 16);
        }
        return Integer.parseInt(colorStr, 16);
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }
}
