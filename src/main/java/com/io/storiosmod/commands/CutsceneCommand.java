package com.io.storiosmod.commands;

import com.io.storiosmod.cutscene.CutsceneStorage;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import com.io.storiosmod.network.CutsceneOpenEditorPacket;
import com.io.storiosmod.network.CutsceneStartPacket;
import com.io.storiosmod.network.CutsceneStopPacket;
import com.io.storiosmod.network.PacketHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collection;

public class CutsceneCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_NAMES = (ctx, builder) ->
            SharedSuggestionProvider.suggest(CutsceneStorage.getNames(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cutscene")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("editor")
                        .executes(CutsceneCommand::openEditor)
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_NAMES)
                                .executes(CutsceneCommand::openEditorWithName)))
                .then(Commands.literal("play")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_NAMES)
                                .executes(CutsceneCommand::playSelf)
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(CutsceneCommand::playTargets))))
                .then(Commands.literal("stop")
                        .executes(CutsceneCommand::stopSelf)
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(CutsceneCommand::stopTargets)))
                .then(Commands.literal("list")
                        .executes(CutsceneCommand::listCutscenes))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(SUGGEST_NAMES)
                                .executes(CutsceneCommand::deleteCutscene))));
    }

    private static int openEditor(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        setSpectatorMode(player);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new CutsceneOpenEditorPacket(null));
        return 1;
    }

    private static int openEditorWithName(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        String name = StringArgumentType.getString(ctx, "name");
        CutsceneTimeline tl = CutsceneStorage.load(name);
        if (tl == null) {
            ctx.getSource().sendFailure(Component.literal("Cutscene '" + name + "' not found."));
            return 0;
        }
        setSpectatorMode(player);
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new CutsceneOpenEditorPacket(tl));
        return 1;
    }

    private static void setSpectatorMode(ServerPlayer player) {
        GameType currentMode = player.gameMode.getGameModeForPlayer();
        if (currentMode != GameType.SPECTATOR) {
            int prevId = currentMode.getId();
            player.setGameMode(GameType.SPECTATOR);
            player.getPersistentData().putInt("storiosmod_prev_gamemode", prevId);
        }
    }

    public static void restoreGameMode(ServerPlayer player) {
        if (!player.getPersistentData().contains("storiosmod_prev_gamemode")) return;
        int prevId = player.getPersistentData().getInt("storiosmod_prev_gamemode");
        player.getPersistentData().remove("storiosmod_prev_gamemode");
        GameType prev = GameType.byId(prevId);
        player.setGameMode(prev);
    }

    public static void setInvulnerable(ServerPlayer player) {
        if (!player.getAbilities().invulnerable) {
            player.getPersistentData().putBoolean("storiosmod_was_vulnerable", true);
            player.getAbilities().invulnerable = true;
            player.onUpdateAbilities();
        }
    }

    public static void restoreInvulnerable(ServerPlayer player) {
        if (player.getPersistentData().getBoolean("storiosmod_was_vulnerable")) {
            player.getAbilities().invulnerable = false;
            player.onUpdateAbilities();
            player.getPersistentData().remove("storiosmod_was_vulnerable");
        }
    }

    private static int playSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        String name = StringArgumentType.getString(ctx, "name");
        CutsceneTimeline tl = CutsceneStorage.load(name);
        if (tl == null) {
            ctx.getSource().sendFailure(Component.literal("Cutscene '" + name + "' not found."));
            return 0;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new CutsceneStartPacket(tl));
        setInvulnerable(player);
        ctx.getSource().sendSuccess(() -> Component.literal("\u00A7aPlaying cutscene '" + name + "'."), true);
        return 1;
    }

    private static int playTargets(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String name = StringArgumentType.getString(ctx, "name");
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        CutsceneTimeline tl = CutsceneStorage.load(name);
        if (tl == null) {
            ctx.getSource().sendFailure(Component.literal("Cutscene '" + name + "' not found."));
            return 0;
        }
        CutsceneStartPacket packet = new CutsceneStartPacket(tl);
        for (ServerPlayer player : targets) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, packet);
            setInvulnerable(player);
        }
        ctx.getSource().sendSuccess(
                () -> Component.literal("\u00A7aPlaying cutscene '" + name + "' for " + targets.size() + " player(s)."),
                true);
        return targets.size();
    }

    private static int stopSelf(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) return 0;
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, new CutsceneStopPacket());
        restoreInvulnerable(player);
        ctx.getSource().sendSuccess(() -> Component.literal("\u00A7aCutscene stopped."), true);
        return 1;
    }

    private static int stopTargets(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
        CutsceneStopPacket packet = new CutsceneStopPacket();
        for (ServerPlayer player : targets) {
            net.neoforged.neoforge.network.PacketDistributor.sendToPlayer((net.minecraft.server.level.ServerPlayer) player, packet);
            restoreInvulnerable(player);
        }
        ctx.getSource().sendSuccess(
                () -> Component.literal("\u00A7aStopped cutscene for " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int listCutscenes(CommandContext<CommandSourceStack> ctx) {
        Collection<String> names = CutsceneStorage.getNames();
        if (names.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("\u00A77No cutscenes saved."), false);
        } else {
            ctx.getSource().sendSuccess(
                    () -> Component.literal("\u00A7eSaved cutscenes: \u00A7f" + String.join(", ", names)), false);
        }
        return names.size();
    }

    private static int deleteCutscene(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (CutsceneStorage.load(name) == null) {
            ctx.getSource().sendFailure(Component.literal("Cutscene '" + name + "' not found."));
            return 0;
        }
        CutsceneStorage.delete(name);
        ctx.getSource().sendSuccess(() -> Component.literal("\u00A7aDeleted cutscene '" + name + "'."), true);
        return 1;
    }
}


