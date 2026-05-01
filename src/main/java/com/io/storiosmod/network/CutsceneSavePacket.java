package com.io.storiosmod.network;

import com.io.storiosmod.cutscene.CutsceneStorage;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CutsceneSavePacket {
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

    public static void handle(CutsceneSavePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasPermissions(2)) {
                CutsceneStorage.save(msg.timeline);
                player.sendSystemMessage(Component.literal("§aCutscene '" + msg.timeline.getName() + "' saved!"));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
