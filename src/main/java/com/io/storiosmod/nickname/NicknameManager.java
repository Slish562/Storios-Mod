package com.io.storiosmod.nickname;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.WeakHashMap;

public class NicknameManager {

    private static final String NBT_KEY = "storios_mod:nickname";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_COLOR_TYPE = "colorType";
    private static final String TAG_COLOR = "color";
    private static final String TAG_START_COLOR = "startColor";
    private static final String TAG_END_COLOR = "endColor";

    private static final WeakHashMap<ServerPlayer, Component> CACHE = new WeakHashMap<>();

    public record NicknameData(String nickname, String colorType, String color, String startColor, String endColor) {

        public static NicknameData solid(String nickname, String color) {
            return new NicknameData(nickname, "solid", color, null, null);
        }

        public static NicknameData gradient(String nickname, String startColor, String endColor) {
            return new NicknameData(nickname, "gradient", null, startColor, endColor);
        }

        public boolean isGradient() {
            return "gradient".equals(colorType);
        }

        public boolean isSolid() {
            return "solid".equals(colorType);
        }
    }

    private static CompoundTag getPersistedData(ServerPlayer player) {
        CompoundTag forgeData = player.getPersistentData();
        if (!forgeData.contains(Player.PERSISTED_NBT_TAG)) {
            forgeData.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        }
        return forgeData.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static void setNickname(ServerPlayer player, NicknameData data) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_NICKNAME, data.nickname());
        tag.putString(TAG_COLOR_TYPE, data.colorType());
        if (data.color() != null) tag.putString(TAG_COLOR, data.color());
        if (data.startColor() != null) tag.putString(TAG_START_COLOR, data.startColor());
        if (data.endColor() != null) tag.putString(TAG_END_COLOR, data.endColor());
        getPersistedData(player).put(NBT_KEY, tag);
        CACHE.remove(player);
        player.refreshDisplayName();
        broadcastTabListUpdate(player);
    }

    public static void removeNickname(ServerPlayer player) {
        getPersistedData(player).remove(NBT_KEY);
        CACHE.remove(player);
        player.refreshDisplayName();
        broadcastTabListUpdate(player);
    }

    public static NicknameData getNickname(ServerPlayer player) {
        CompoundTag persistent = getPersistedData(player);
        if (!persistent.contains(NBT_KEY)) return null;
        CompoundTag tag = persistent.getCompound(NBT_KEY);
        return new NicknameData(
                tag.getString(TAG_NICKNAME),
                tag.getString(TAG_COLOR_TYPE),
                tag.contains(TAG_COLOR) ? tag.getString(TAG_COLOR) : null,
                tag.contains(TAG_START_COLOR) ? tag.getString(TAG_START_COLOR) : null,
                tag.contains(TAG_END_COLOR) ? tag.getString(TAG_END_COLOR) : null
        );
    }

    public static boolean hasNickname(ServerPlayer player) {
        return getPersistedData(player).contains(NBT_KEY);
    }

    public static Component getCachedDisplayName(ServerPlayer player) {
        return CACHE.computeIfAbsent(player, NicknameManager::buildDisplayName);
    }

    public static Component buildDisplayName(ServerPlayer player) {
        NicknameData data = getNickname(player);
        if (data == null) return null;

        if (data.isGradient()) {
            return createGradient(data.nickname(), parseColor(data.startColor()), parseColor(data.endColor()));
        } else if (data.isSolid()) {
            return createSolid(data.nickname(), parseColor(data.color()));
        }
        return Component.literal(data.nickname());
    }

    public static void broadcastTabListUpdate(ServerPlayer player) {
        if (player.getServer() == null) return;
        Component displayName = getCachedDisplayName(player);
        try {
            java.lang.reflect.Field field = net.minecraft.server.level.ServerPlayer.class.getDeclaredField("tabListDisplayName");
            field.setAccessible(true);
            field.set(player, displayName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player
        );
        player.getServer().getPlayerList().broadcastAll(packet);
    }

    private static Component createGradient(String text, int startColor, int endColor) {
        MutableComponent root = Component.literal("");
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (length > 1) ? (float) i / (float) (length - 1) : 0;
            int interpolated = interpolateColor(startColor, endColor, ratio);

            Style style = Style.EMPTY.withColor(TextColor.fromRgb(interpolated));
            root.append(Component.literal(String.valueOf(text.charAt(i))).setStyle(style));
        }

        return root;
    }

    private static Component createSolid(String text, int color) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(color));
        return Component.literal(text).setStyle(style);
    }

    private static int parseColor(String colorStr) {
        if (colorStr.startsWith("#")) {
            return Integer.parseInt(colorStr.substring(1), 16);
        }
        return Integer.parseInt(colorStr, 16);
    }

    private static int interpolateColor(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);

        return (r << 16) | (g << 8) | b;
    }
}

