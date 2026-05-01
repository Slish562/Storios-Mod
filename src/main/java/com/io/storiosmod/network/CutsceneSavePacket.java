package com.io.storiosmod.network;

import com.io.storiosmod.cutscene.CutsceneStorage;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CutsceneSavePacket implements CustomPacketPayload {
    public static final Type<CutsceneSavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "cutscene_save_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, CutsceneSavePacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CutsceneSavePacket::decode);
    private final CutsceneTimeline timeline;

    public CutsceneSavePacket(CutsceneTimeline timeline) {
        this.timeline = timeline;
    }

    public static void encode(CutsceneSavePacket msg, FriendlyByteBuf buf) {
        msg.timeline.encode(buf);
    }

    public static CutsceneSavePacket decode(FriendlyByteBuf buf) {
        return new CutsceneSavePacket(CutsceneTimeline.decode(buf));
    }

    public static void handle(CutsceneSavePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ((net.minecraft.server.level.ServerPlayer) ctx.player());
            if (player != null && player.hasPermissions(2)) {
                CutsceneStorage.save(msg.timeline);
                player.sendSystemMessage(Component.literal("\u00A7aCutscene '" + msg.timeline.getName() + "' saved!"));
            }
        });

    }
}


