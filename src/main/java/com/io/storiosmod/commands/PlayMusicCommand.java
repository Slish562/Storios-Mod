package com.io.storiosmod.commands;

import com.io.storiosmod.network.PacketHandler;
import com.io.storiosmod.network.PlayMusicPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collection;
import java.util.Set;

public class PlayMusicCommand {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".ogg", ".wav", ".mp3");
    private static final String[] SOUND_SOURCES = {
            "master", "music", "record", "weather", "block",
            "hostile", "neutral", "player", "ambient", "voice"
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("playmusic")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("source", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    for (String s : SOUND_SOURCES)
                                        builder.suggest(s);
                                    return builder.buildFuture();
                                })
                                .then(Commands.argument("volume", FloatArgumentType.floatArg(0.0f, 1.0f))
                                        .then(Commands.argument("url", StringArgumentType.greedyString())
                                                .executes(PlayMusicCommand::execute))))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        try {
            Collection<ServerPlayer> targets = EntityArgument.getPlayers(context, "targets");
            String source = StringArgumentType.getString(context, "source");
            float volume = FloatArgumentType.getFloat(context, "volume");
            String url = StringArgumentType.getString(context, "url");

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                context.getSource().sendFailure(Component.literal("Only http and https URLs are allowed."));
                return 0;
            }

            String extension = extractExtension(url);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                context.getSource().sendFailure(
                        Component.literal("Only audio files are allowed (.ogg, .wav, .mp3)."));
                return 0;
            }

            PlayMusicPacket packet = new PlayMusicPacket(url, source, volume);
            for (ServerPlayer player : targets) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
            }

            context.getSource().sendSuccess(
                    () -> Component.literal("Playing music (vol=" + volume + ", " + source + ") for "
                            + targets.size() + " player(s)."),
                    true);
            return targets.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("An error occurred: " + e.getMessage()));
            return 0;
        }
    }

    private static String extractExtension(String url) {
        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }
        int hashIndex = path.indexOf('#');
        if (hashIndex != -1) {
            path = path.substring(0, hashIndex);
        }
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex != -1) {
            return path.substring(dotIndex).toLowerCase();
        }
        return "";
    }
}
