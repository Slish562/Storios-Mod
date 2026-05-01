package com.io.storiosmod.network;

import com.io.storiosmod.client.ClientMusicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class StopMusicPacket implements CustomPacketPayload {
    public static final Type<StopMusicPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "stop_music_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, StopMusicPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), StopMusicPacket::decode);

    public StopMusicPacket() {
    }

    public static void encode(StopMusicPacket msg, FriendlyByteBuf buf) {
    }

    public static StopMusicPacket decode(FriendlyByteBuf buf) {
        return new StopMusicPacket();
    }

    public static void handle(StopMusicPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientMusicHandler.stop();
        });

    }
}

