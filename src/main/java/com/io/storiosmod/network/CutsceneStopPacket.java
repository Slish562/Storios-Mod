package com.io.storiosmod.network;

import com.io.storiosmod.client.CutsceneCameraHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CutsceneStopPacket implements CustomPacketPayload {
    public static final Type<CutsceneStopPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "cutscene_stop_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CutsceneStopPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CutsceneStopPacket::decode);

    public CutsceneStopPacket() {
    }

    public static void encode(CutsceneStopPacket msg, net.minecraft.network.RegistryFriendlyByteBuf buf) {
    }

    public static CutsceneStopPacket decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
        return new CutsceneStopPacket();
    }

    public static void handle(CutsceneStopPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CutsceneCameraHandler.stop();
        });

    }
}



