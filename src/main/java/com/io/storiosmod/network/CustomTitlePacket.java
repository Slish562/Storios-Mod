package com.io.storiosmod.network;

import com.io.storiosmod.client.ClientTitleHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CustomTitlePacket {
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

    public static void encode(CustomTitlePacket msg, FriendlyByteBuf buf) {
        buf.writeComponent(msg.message);
        buf.writeUtf(msg.vAnchor);
        buf.writeInt(msg.vOffset);
        buf.writeUtf(msg.hAnchor);
        buf.writeInt(msg.hOffset);
        buf.writeFloat(msg.scale);
        buf.writeInt(msg.fadeIn);
        buf.writeInt(msg.stay);
        buf.writeInt(msg.fadeOut);
    }

    public static CustomTitlePacket decode(FriendlyByteBuf buf) {
        return new CustomTitlePacket(
                buf.readComponent(),
                buf.readUtf(),
                buf.readInt(),
                buf.readUtf(),
                buf.readInt(),
                buf.readFloat(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt());
    }

    public static void handle(CustomTitlePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTitleHandler.setCustomTitle(
                    msg.message, msg.vAnchor, msg.vOffset, msg.hAnchor, msg.hOffset, msg.scale, msg.fadeIn, msg.stay,
                    msg.fadeOut));
        });
        ctx.get().setPacketHandled(true);
    }
}
