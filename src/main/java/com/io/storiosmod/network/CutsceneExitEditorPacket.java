package com.io.storiosmod.network;

import com.io.storiosmod.commands.CutsceneCommand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CutsceneExitEditorPacket implements CustomPacketPayload {
    public static final Type<CutsceneExitEditorPacket> TYPE = new Type<>(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.io.storiosmod.StoriosMod.MODID, "cutscene_exit_editor_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, CutsceneExitEditorPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CutsceneExitEditorPacket::decode);

    public CutsceneExitEditorPacket() {
    }

    public static void encode(CutsceneExitEditorPacket msg, FriendlyByteBuf buf) {
    }

    public static CutsceneExitEditorPacket decode(FriendlyByteBuf buf) {
        return new CutsceneExitEditorPacket();
    }

    public static void handle(CutsceneExitEditorPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ((net.minecraft.server.level.ServerPlayer) ctx.player());
            if (player != null) {
                CutsceneCommand.restoreGameMode(player);
            }
        });

    }
}

