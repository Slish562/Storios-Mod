package com.io.storiosmod.network;

import com.io.storiosmod.client.ClientMusicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayMusicPacket {
    private final String url;
    private final String category;
    private final float volume;

    public PlayMusicPacket(String url, String category, float volume) {
        this.url = url;
        this.category = category;
        this.volume = volume;
    }

    public static void encode(PlayMusicPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.url);
        buf.writeUtf(msg.category);
        buf.writeFloat(msg.volume);
    }

    public static PlayMusicPacket decode(FriendlyByteBuf buf) {
        return new PlayMusicPacket(buf.readUtf(), buf.readUtf(), buf.readFloat());
    }

    public static void handle(PlayMusicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> ClientMusicHandler.play(msg.url, msg.category, msg.volume));
        });
        ctx.get().setPacketHandled(true);
    }
}
