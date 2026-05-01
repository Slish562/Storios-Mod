package com.io.storiosmod.commands;

import com.io.storiosmod.network.PacketHandler;
import com.io.storiosmod.network.StopMusicPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;

public class StopMusicCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stopmusic")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(StopMusicCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");

            StopMusicPacket packet = new StopMusicPacket();
            for (ServerPlayer player : targets) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }

            context.getSource().sendSuccess(
                    () -> Component.literal("Stopped music for " + targets.size() + " player(s)."), true);
            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }
}
