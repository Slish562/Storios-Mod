package com.io.storiosmod.network;

import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class RadiusTriggerResetPacket implements CustomPacketPayload {
    public static final Type<RadiusTriggerResetPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "radius_trigger_reset_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, RadiusTriggerResetPacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), RadiusTriggerResetPacket::decode);
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

    public static void handle(RadiusTriggerResetPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ((net.minecraft.server.level.ServerPlayer) ctx.player());
            if (player == null || !player.hasPermissions(2)) return;

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (be instanceof RadiusTriggerBlockEntity trigger) {
                trigger.resetTriggered();

                net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(msg.pos);
                player.level().sendBlockUpdated(msg.pos, state, state, 3);
            }
        });

    }
}

