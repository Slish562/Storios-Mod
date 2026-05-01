package com.io.storiosmod.network;

import com.io.storiosmod.StoriosMod;
import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity;
import com.io.storiosmod.block.entity.RadiusTriggerBlockEntity.TriggerMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RadiusTriggerSavePacket implements CustomPacketPayload {
    public static final Type<RadiusTriggerSavePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(StoriosMod.MODID, "radius_trigger_save_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<FriendlyByteBuf, RadiusTriggerSavePacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), RadiusTriggerSavePacket::decode);
    private final BlockPos pos;
    private final int radius;
    private final TriggerMode mode;
    private final List<String> commands;

    public RadiusTriggerSavePacket(BlockPos pos, int radius, TriggerMode mode, List<String> commands) {
        this.pos = pos;
        this.radius = radius;
        this.mode = mode;
        this.commands = commands;
    }

    public static void encode(RadiusTriggerSavePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.radius);
        buf.writeUtf(msg.mode.name());
        buf.writeInt(msg.commands.size());
        for (String command : msg.commands) {
            buf.writeUtf(command);
        }
    }

    public static RadiusTriggerSavePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int radius = buf.readInt();
        TriggerMode mode;
        try { mode = TriggerMode.valueOf(buf.readUtf()); }
        catch (Exception e) { mode = TriggerMode.ONCE; }

        int count = buf.readInt();
        List<String> commands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            commands.add(buf.readUtf());
        }
        return new RadiusTriggerSavePacket(pos, radius, mode, commands);
    }

    public static void handle(RadiusTriggerSavePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ((net.minecraft.server.level.ServerPlayer) ctx.player());
            if (player == null || !player.hasPermissions(2)) return;

            BlockEntity be = player.level().getBlockEntity(msg.pos);
            if (be instanceof RadiusTriggerBlockEntity trigger) {
                trigger.setRadius(msg.radius);
                trigger.setTriggerMode(msg.mode);
                trigger.setCommands(msg.commands);
                trigger.setChanged();

                net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(msg.pos);
                player.level().sendBlockUpdated(msg.pos, state, state, 3);
            }
        });

    }
}

