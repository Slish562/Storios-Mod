package com.io.storiosmod.network;

import com.io.storiosmod.client.CutsceneEditorManager;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CutsceneOpenEditorPacket {

    private final CutsceneTimeline timeline;

    public CutsceneOpenEditorPacket(CutsceneTimeline timeline) {
        this.timeline = timeline;
    }

    public static void encode(CutsceneOpenEditorPacket msg, FriendlyByteBuf buf) {
        if (msg.timeline != null) {
            buf.writeBoolean(true);
            msg.timeline.encode(buf);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static CutsceneOpenEditorPacket decode(FriendlyByteBuf buf) {
        boolean hasTimeline = buf.readBoolean();
        CutsceneTimeline tl = hasTimeline ? CutsceneTimeline.decode(buf) : null;
        return new CutsceneOpenEditorPacket(tl);
    }

    public static void handle(CutsceneOpenEditorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> {
                        CutsceneTimeline tl = msg.timeline != null ? msg.timeline : new CutsceneTimeline("new");
                        CutsceneEditorManager.enterEditor(tl);
                    });
        });
        ctx.get().setPacketHandled(true);
    }
}
