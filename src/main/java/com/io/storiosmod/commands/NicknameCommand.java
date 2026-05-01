package com.io.storiosmod.commands;

import com.io.storiosmod.nickname.NicknameManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class NicknameCommand {

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
                dispatcher.register(buildCommand("nickname"));
                dispatcher.register(buildCommand("nk"));
        }

        private static LiteralArgumentBuilder<CommandSourceStack> buildCommand(String name) {
                return Commands.literal(name)
                                .then(Commands.literal("set")
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                                .requires(source -> source.hasPermission(2))
                                                                .then(Commands.literal("solid")
                                                                                .then(Commands.argument("color",
                                                                                                StringArgumentType
                                                                                                                .string())
                                                                                                .then(Commands.argument(
                                                                                                                "name",
                                                                                                                StringArgumentType
                                                                                                                                .greedyString())
                                                                                                                .executes(ctx -> setSolid(
                                                                                                                                ctx,
                                                                                                                                EntityArgument.getPlayers(
                                                                                                                                                ctx,
                                                                                                                                                "targets"))))))
                                                                .then(Commands.literal("gradient")
                                                                                .then(Commands.argument("startColor",
                                                                                                StringArgumentType
                                                                                                                .string())
                                                                                                .then(Commands.argument(
                                                                                                                "endColor",
                                                                                                                StringArgumentType
                                                                                                                                .string())
                                                                                                                .then(Commands.argument(
                                                                                                                                "name",
                                                                                                                                StringArgumentType
                                                                                                                                                .greedyString())
                                                                                                                                .executes(ctx -> setGradient(
                                                                                                                                                ctx,
                                                                                                                                                EntityArgument.getPlayers(
                                                                                                                                                                ctx,
                                                                                                                                                                "targets"))))))))
                                                .then(Commands.literal("solid")
                                                                .then(Commands.argument("color",
                                                                                StringArgumentType.string())
                                                                                .then(Commands.argument("name",
                                                                                                StringArgumentType
                                                                                                                .greedyString())
                                                                                                .executes(ctx -> setSolid(
                                                                                                                ctx,
                                                                                                                null)))))
                                                .then(Commands.literal("gradient")
                                                                .then(Commands.argument("startColor",
                                                                                StringArgumentType.string())
                                                                                .then(Commands.argument("endColor",
                                                                                                StringArgumentType
                                                                                                                .string())
                                                                                                .then(Commands.argument(
                                                                                                                "name",
                                                                                                                StringArgumentType
                                                                                                                                .greedyString())
                                                                                                                .executes(ctx -> setGradient(
                                                                                                                                ctx,
                                                                                                                                null)))))))
                                .then(Commands.literal("reset")
                                                .then(Commands.argument("targets", EntityArgument.players())
                                                                .requires(source -> source.hasPermission(2))
                                                                .executes(NicknameCommand::resetOther))
                                                .executes(NicknameCommand::resetSelf));
        }

        private static int setSolid(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
                try {
                        String colorStr = StringArgumentType.getString(ctx, "color");
                        String name = StringArgumentType.getString(ctx, "name");

                        validateColor(colorStr);
                        NicknameManager.NicknameData data = NicknameManager.NicknameData.solid(name, colorStr);

                        if (targets != null && !targets.isEmpty()) {
                                for (ServerPlayer target : targets) {
                                        NicknameManager.setNickname(target, data);
                                        ctx.getSource().sendSuccess(() -> Component.literal("Set nickname for ")
                                                        .append(target.getName())
                                                        .append(Component.literal(" to "))
                                                        .append(NicknameManager.buildDisplayName(target)),
                                                        true);
                                }
                                return targets.size();
                        } else {
                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                NicknameManager.setNickname(player, data);
                                ctx.getSource().sendSuccess(() -> Component.literal("Your nickname is now ")
                                                .append(NicknameManager.buildDisplayName(player)), false);
                                return 1;
                        }
                } catch (IllegalArgumentException e) {
                        ctx.getSource().sendFailure(
                                        Component.literal("§cInvalid color format! Use hex codes like #FF0000"));
                        return 0;
                } catch (Exception e) {
                        ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
                        return 0;
                }
        }

        private static int setGradient(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> targets) {
                try {
                        String startColorStr = StringArgumentType.getString(ctx, "startColor");
                        String endColorStr = StringArgumentType.getString(ctx, "endColor");
                        String name = StringArgumentType.getString(ctx, "name");

                        validateColor(startColorStr);
                        validateColor(endColorStr);
                        NicknameManager.NicknameData data = NicknameManager.NicknameData.gradient(name, startColorStr, endColorStr);

                        if (targets != null && !targets.isEmpty()) {
                                for (ServerPlayer target : targets) {
                                        NicknameManager.setNickname(target, data);
                                        ctx.getSource().sendSuccess(() -> Component.literal("Set nickname for ")
                                                        .append(target.getName())
                                                        .append(Component.literal(" to "))
                                                        .append(NicknameManager.buildDisplayName(target)),
                                                        true);
                                }
                                return targets.size();
                        } else {
                                ServerPlayer player = ctx.getSource().getPlayerOrException();
                                NicknameManager.setNickname(player, data);
                                ctx.getSource().sendSuccess(() -> Component.literal("Your nickname is now ")
                                                .append(NicknameManager.buildDisplayName(player)), false);
                                return 1;
                        }
                } catch (IllegalArgumentException e) {
                        ctx.getSource().sendFailure(
                                        Component.literal("§cInvalid color format! Use hex codes like #FF0000"));
                        return 0;
                } catch (Exception e) {
                        ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
                        return 0;
                }
        }

        private static int resetSelf(CommandContext<CommandSourceStack> ctx) {
                try {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        NicknameManager.removeNickname(player);
                        ctx.getSource().sendSuccess(() -> Component.literal("Your nickname has been reset"), false);
                        return 1;
                } catch (Exception e) {
                        ctx.getSource().sendFailure(Component.literal("§cError: " + e.getMessage()));
                        return 0;
                }
        }

        private static int resetOther(CommandContext<CommandSourceStack> ctx) {
                try {
                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
                        for (ServerPlayer target : targets) {
                                NicknameManager.removeNickname(target);
                                ctx.getSource().sendSuccess(() -> Component.literal("Reset nickname for ")
                                                .append(target.getName()), true);
                        }
                        return targets.size();
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
