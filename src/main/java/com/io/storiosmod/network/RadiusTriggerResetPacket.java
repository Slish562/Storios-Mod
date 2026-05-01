package com.io.storiosmod.network;

import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RadiusTriggerResetPacket {
    private final BlockPos pos;

    public RadiusTriggerResetPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(RadiusTriggerResetPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static RadiusTriggerResetPacket decode(FriendlyByteBuf buf) {
        return new RadiusTriggerResetPacket(buf.readBlockPos());
    }

    public static void handle(RadiusTriggerResetPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(2)) return;

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (be instanceof RadiusTriggerBlockEntity trigger) {
                trigger.resetTriggered();

                net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(msg.pos);
                player.level().sendBlockUpdated(msg.pos, state, state, 3);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
