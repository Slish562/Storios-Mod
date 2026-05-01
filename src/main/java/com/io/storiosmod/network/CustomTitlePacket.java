package com.io.storiosmod.network;

import com.io.storiosmod.client.ClientTitleHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import java.util.function.Supplier;

public class CustomTitlePacket implements CustomPacketPayload {
    public static final Type<CustomTitlePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("storiosmod", "custom_title_packet"));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, CustomTitlePacket> STREAM_CODEC = StreamCodec.of((buf, msg) -> encode(msg, buf), CustomTitlePacket::decode);
    private final Component message;
    private final String vAnchor;
    private final int vOffset;
    private final String hAnchor;
    private final int hOffset;
    private final float scale;
    private final int fadeIn;
    private final int stay;
    private final int fadeOut;

    public CustomTitlePacket(Component message, String vAnchor, int vOffset, String hAnchor, int hOffset, float scale,
            int fadeIn, int stay, int fadeOut) {
        this.message = message;
        this.vAnchor = vAnchor;
        this.vOffset = vOffset;
        this.hAnchor = hAnchor;
        this.hOffset = hOffset;
        this.scale = scale;
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
    }

    public static void encode(CustomTitlePacket msg, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.encode(buf, msg.message);
        buf.writeUtf(msg.vAnchor);
        buf.writeInt(msg.vOffset);
        buf.writeUtf(msg.hAnchor);
        buf.writeInt(msg.hOffset);
        buf.writeFloat(msg.scale);
        buf.writeInt(msg.fadeIn);
        buf.writeInt(msg.stay);
        buf.writeInt(msg.fadeOut);
    }

    public static CustomTitlePacket decode(net.minecraft.network.RegistryFriendlyByteBuf buf) {
        return new CustomTitlePacket(
                net.minecraft.network.chat.ComponentSerialization.STREAM_CODEC.decode(buf),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readInt(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt());
    }

    public static void handle(CustomTitlePacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ClientTitleHandler.setCustomTitle(
                    msg.message, msg.vAnchor, msg.vOffset, msg.hAnchor, msg.hOffset, msg.scale, msg.fadeIn, msg.stay,
                    msg.fadeOut);
        });

    }
}



