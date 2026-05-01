package com.io.storiosmod.network;

import com.io.storiosmod.commands.CutsceneCommand;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CutsceneExitEditorPacket {

    public CutsceneExitEditorPacket() {
    }

    public static void encode(CutsceneExitEditorPacket msg, FriendlyByteBuf buf) {
    }

    public static CutsceneExitEditorPacket decode(FriendlyByteBuf buf) {
        return new CutsceneExitEditorPacket();
    }

    public static void handle(CutsceneExitEditorPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CutsceneCommand.restoreGameMode(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
