package com.io.storiosmod.network;

import com.io.storiosmod.commands.CutsceneCommand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CutsceneFinishedPacket {

    public CutsceneFinishedPacket() {}

    public static void encode(CutsceneFinishedPacket msg, FriendlyByteBuf buf) {}

    public static CutsceneFinishedPacket decode(FriendlyByteBuf buf) {
        return new CutsceneFinishedPacket();
    }

    public static void handle(CutsceneFinishedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CutsceneCommand.restoreInvulnerable(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
