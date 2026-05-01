package com.io.storiosmod.network;

import com.io.storiosmod.commands.CutsceneCommand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CutsceneFinishedPacket implements CustomPacketPayload {
    public static final Type<CutsceneFinishedPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "cutscene_finished_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, CutsceneFinishedPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CutsceneFinishedPacket::decode);

    public CutsceneFinishedPacket() {}

    public static void encode(CutsceneFinishedPacket msg, FriendlyByteBuf buf) {}

    public static CutsceneFinishedPacket decode(FriendlyByteBuf buf) {
        return new CutsceneFinishedPacket();
    }

    public static void handle(CutsceneFinishedPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ((net.minecraft.server.level.ServerPlayer) ctx.player());
            if (player != null) {
                CutsceneCommand.restoreInvulnerable(player);
            }
        });

    }
}

