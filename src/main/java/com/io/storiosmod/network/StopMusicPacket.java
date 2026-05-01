package com.io.storiosmod.network;

import com.io.storiosmod.client.ClientMusicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class StopMusicPacket {

    public StopMusicPacket() {
    }

    public static void encode(StopMusicPacket msg, FriendlyByteBuf buf) {
    }

    public static StopMusicPacket decode(FriendlyByteBuf buf) {
        return new StopMusicPacket();
    }

    public static void handle(StopMusicPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientMusicHandler.stop());
        });
        ctx.get().setPacketHandled(true);
    }
}
