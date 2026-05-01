package com.io.storiosmod.network;

import com.io.storiosmod.client.CutsceneCameraHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CutsceneStopPacket {

    public CutsceneStopPacket() {
    }

    public static void encode(CutsceneStopPacket msg, FriendlyByteBuf buf) {
    }

    public static CutsceneStopPacket decode(FriendlyByteBuf buf) {
        return new CutsceneStopPacket();
    }

    public static void handle(CutsceneStopPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> CutsceneCameraHandler.stop());
        });
        ctx.get().setPacketHandled(true);
    }
}
