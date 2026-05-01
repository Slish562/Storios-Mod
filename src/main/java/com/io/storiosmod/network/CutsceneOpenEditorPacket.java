package com.io.storiosmod.network;

import com.io.storiosmod.client.CutsceneEditorManager;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CutsceneOpenEditorPacket implements CustomPacketPayload {
    public static final Type<CutsceneOpenEditorPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "cutscene_open_editor_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CutsceneOpenEditorPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CutsceneOpenEditorPacket::decode);

    private final CutsceneTimeline timeline;

    public CutsceneOpenEditorPacket(CutsceneTimeline timeline) {
        this.timeline = timeline;
    }

    public static void encode(CutsceneOpenEditorPacket msg, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        if (msg.timeline != null) {
            buf.writeBoolean(true);
            msg.timeline.encode(buf);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static CutsceneOpenEditorPacket decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
        boolean hasTimeline = buf.readBoolean();
        CutsceneTimeline tl = hasTimeline ? CutsceneTimeline.decode(buf) : null;
        return new CutsceneOpenEditorPacket(tl);
    }

    public static void handle(CutsceneOpenEditorPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            CutsceneTimeline tl = msg.timeline != null ? msg.timeline : new CutsceneTimeline("new");
            CutsceneEditorManager.enterEditor(tl);
        });

    }
}



