package com.io.storiosmod.commands;

import com.io.storiosmod.network.CustomTitlePacket;
import com.io.storiosmod.network.PacketHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

public class GradientTitleCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gtitle")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.literal("custom")
                                .then(Commands.argument("vAnchor", StringArgumentType.word()) // top, center, bottom
                                        .suggests((ctx, b) -> {
                                            b.suggest("top");
                                            b.suggest("center");
                                            b.suggest("bottom");
                                            return b.buildFuture();
                                        })
                                        .then(Commands.argument("vOffset", IntegerArgumentType.integer())
                                                .then(Commands.argument("hAnchor", StringArgumentType.word()) // left,
                                                                                                              // center,
                                                                                                              // right
                                                        .suggests((ctx, b) -> {
                                                            b.suggest("left");
                                                            b.suggest("center");
                                                            b.suggest("right");
                                                            return b.buildFuture();
                                                        })
                                                        .then(Commands
                                                                .argument("hOffset", IntegerArgumentType.integer())
                                                                .then(Commands
                                                                        .argument("scale",
                                                                                FloatArgumentType.floatArg(0.1f, 10.0f))
                                                                        .then(Commands
                                                                                .argument("fadeIn",
                                                                                        IntegerArgumentType.integer(0))
                                                                                .then(Commands
                                                                                        .argument("stay",
                                                                                                IntegerArgumentType
                                                                                                        .integer(0))
                                                                                        .then(Commands.argument(
                                                                                                "fadeOut",
                                                                                                IntegerArgumentType
                                                                                                        .integer(0))
                                                                                                .then(Commands.literal(
                                                                                                        "gradient")
                                                                                                        .then(Commands
                                                                                                                .argument(
                                                                                                                        "startColor",
                                                                                                                        StringArgumentType
                                                                                                                                .string())
                                                                                                                .then(Commands
                                                                                                                        .argument(
                                                                                                                                "endColor",
                                                                                                                                StringArgumentType
                                                                                                                                        .string())
                                                                                                                        .then(Commands
                                                                                                                                .argument(
                                                                                                                                        "message",
                                                                                                                                        StringArgumentType
                                                                                                                                                .greedyString())
                                                                                                                                .executes(
                                                                                                                                        ctx -> executeCustom(
                                                                                                                                                ctx,
                                                                                                                                                true)))))
                                                                                                        .then(Commands
                                                                                                                .literal(
                                                                                                                        "solid")
                                                                                                                .then(Commands
                                                                                                                        .argument(
                                                                                                                                "color",
                                                                                                                                StringArgumentType
                                                                                                                                        .string())
                                                                                                                        .then(Commands
                                                                                                                                .argument(
                                                                                                                                        "message",
                                                                                                                                        StringArgumentType
                                                                                                                                                .greedyString())
                                                                                                                                .executes(
                                                                                                                                        ctx -> executeCustom(
                                                                                                                                                ctx,
                                                                                                                                                false)))))))))))))
                                        .then(Commands.argument("location", StringArgumentType.word()) // title,
                                                                                                       // subtitle,
                                                                                                       // actionbar
                                                .suggests((context, builder) -> {
                                                    builder.suggest("title");
                                                    builder.suggest("subtitle");
                                                    builder.suggest("actionbar");
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("fadeIn", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("stay", IntegerArgumentType.integer(0))
                                                                .then(Commands
                                                                        .argument("fadeOut",
                                                                                IntegerArgumentType.integer(0))
                                                                        .then(Commands.literal("gradient")
                                                                                .then(Commands
                                                                                        .argument("startColor",
                                                                                                StringArgumentType
                                                                                                        .string())
                                                                                        .then(Commands.argument(
                                                                                                "endColor",
                                                                                                StringArgumentType
                                                                                                        .string())
                                                                                                .then(Commands.argument(
                                                                                                        "message",
                                                                                                        StringArgumentType
                                                                                                                .greedyString())
                                                                                                        .executes(
                                                                                                                GradientTitleCommand::executeGradient)))))
                                                                        .then(Commands.literal("solid")
                                                                                .then(Commands
                                                                                        .argument("color",
                                                                                                StringArgumentType
                                                                                                        .string())
                                                                                        .then(Commands.argument(
                                                                                                "message",
                                                                                                StringArgumentType
                                                                                                        .greedyString())
                                                                                                .executes(
                                                                                                        GradientTitleCommand::executeSolid))))))))))));
    }

    private static int executeCustom(CommandContext<CommandSourceStack> context, boolean isGradient) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String vAnchor = StringArgumentType.getString(context, "vAnchor");
            int vOffset = IntegerArgumentType.getInteger(context, "vOffset");
            String hAnchor = StringArgumentType.getString(context, "hAnchor");
            int hOffset = IntegerArgumentType.getInteger(context, "hOffset");
            float scale = FloatArgumentType.getFloat(context, "scale");
            int fadeIn = IntegerArgumentType.getInteger(context, "fadeIn");
            int stay = IntegerArgumentType.getInteger(context, "stay");
            int fadeOut = IntegerArgumentType.getInteger(context, "fadeOut");
            String messageText = StringArgumentType.getString(context, "message");

            Component messageComponent;
            if (isGradient) {
                String startColorStr = StringArgumentType.getString(context, "startColor");
                String endColorStr = StringArgumentType.getString(context, "endColor");
                messageComponent = createGradient(messageText, parseColor(startColorStr), parseColor(endColorStr));
            } else {
                String colorStr = StringArgumentType.getString(context, "color");
                messageComponent = createSolid(messageText, parseColor(colorStr));
            }

            CustomTitlePacket packet = new CustomTitlePacket(messageComponent, vAnchor, vOffset, hAnchor, hOffset,
                    scale, fadeIn, stay, fadeOut);

            for (ServerPlayer player : targets) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }

            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeGradient(CommandContext<CommandSourceStack> context) {
        try {
            return sendTitle(context, true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeSolid(CommandContext<CommandSourceStack> context) {
        try {
            return sendTitle(context, false);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }

    private static int sendTitle(CommandContext<CommandSourceStack> context, boolean isGradient) throws Exception {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
        String location = StringArgumentType.getString(context, "location");
        int fadeIn = IntegerArgumentType.getInteger(context, "fadeIn");
        int stay = IntegerArgumentType.getInteger(context, "stay");
        int fadeOut = IntegerArgumentType.getInteger(context, "fadeOut");
        String messageText = StringArgumentType.getString(context, "message");

        Component messageComponent;
        if (isGradient) {
            String startColorStr = StringArgumentType.getString(context, "startColor");
            String endColorStr = StringArgumentType.getString(context, "endColor");
            messageComponent = createGradient(messageText, parseColor(startColorStr), parseColor(endColorStr));
        } else {
            String colorStr = StringArgumentType.getString(context, "color");
            messageComponent = createSolid(messageText, parseColor(colorStr));
        }

        for (ServerPlayer player : targets) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));

            switch (location.toLowerCase()) {
                case "title":
                    player.connection.send(new ClientboundSetTitleTextPacket(messageComponent));
                    break;
                case "subtitle":
                    player.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
                    player.connection.send(new ClientboundSetSubtitleTextPacket(messageComponent));
                    break;
                case "actionbar":
                    player.connection.send(new ClientboundSetActionBarTextPacket(messageComponent));
                    break;
                default:
                    player.connection.send(new ClientboundSetTitleTextPacket(messageComponent));
                    break;
            }
        }
        return targets.size();
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
