package com.io.storiosmod.network;

import com.io.storiosmod.client.CutsceneCameraHandler;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CutsceneStartPacket {
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

    public static void handle(CutsceneStartPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> CutsceneCameraHandler.start(msg.timeline));
        });
        ctx.get().setPacketHandled(true);
    }
}
