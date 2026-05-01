package com.io.storiosmod.network;

import com.io.storiosmod.client.CutsceneCameraHandler;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CutsceneStartPacket implements CustomPacketPayload {
    public static final Type<CutsceneStartPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "cutscene_start_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, CutsceneStartPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CutsceneStartPacket::decode);
    private final CutsceneTimeline timeline;

    public CutsceneStartPacket(CutsceneTimeline timeline) {
        this.timeline = timeline;
    }

    public static void encode(CutsceneStartPacket msg, FriendlyByteBuf buf) {
        msg.timeline.encode(buf);
    }

    public static CutsceneStartPacket decode(FriendlyByteBuf buf) {
        return new CutsceneStartPacket(CutsceneTimeline.decode(buf));
    }

    public static void handle(CutsceneStartPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CutsceneCameraHandler.start(msg.timeline);
        });

    }
}

