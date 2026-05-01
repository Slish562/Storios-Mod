package com.io.storiosmod.network;

import com.io.storiosmod.client.ClientMusicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class PlayMusicPacket implements CustomPacketPayload {
    public static final Type<PlayMusicPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "play_music_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, PlayMusicPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), PlayMusicPacket::decode);
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

    public static void handle(PlayMusicPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientMusicHandler.play(msg.url, msg.category, msg.volume);
        });

    }
}

