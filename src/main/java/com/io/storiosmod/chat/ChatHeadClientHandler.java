package com.io.storiosmod.chat;

import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;

import java.util.UUID;

public class ChatHeadClientHandler {

    private static boolean figuraLoaded = false;
    private static boolean checked = false;

    private static boolean reflectionReady = false;
    private static boolean reflectionAttempted = false;
    private static java.lang.reflect.Method mGetAvatarForPlayer;
    private static java.lang.reflect.Field fRenderer;
    private static java.lang.reflect.Method mGetTexture;
    private static java.lang.reflect.Method mGetWidth;
    private static java.lang.reflect.Method mGetHeight;

    public static boolean isFiguraLoaded() {
        if (!checked) {
            figuraLoaded = ModList.get().isLoaded("figura");
            checked = true;
        }
        return figuraLoaded;
    }

    private static void initReflection() {
        if (reflectionAttempted)
            return;
        reflectionAttempted = true;
        try {
            Class<?> avatarManagerClass = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            mGetAvatarForPlayer = avatarManagerClass.getMethod("getAvatarForPlayer", UUID.class);
            Class<?> avatarClass = Class.forName("org.figuramc.figura.avatar.Avatar");
            fRenderer = avatarClass.getField("renderer");
            Class<?> avatarRendererClass = Class.forName("org.figuramc.figura.model.rendering.AvatarRenderer");
            mGetTexture = avatarRendererClass.getMethod("getTexture", String.class);
            Class<?> figuraTextureClass = Class.forName("org.figuramc.figura.model.rendering.texture.FiguraTexture");
            mGetWidth = figuraTextureClass.getMethod("getWidth");
            mGetHeight = figuraTextureClass.getMethod("getHeight");
            reflectionReady = true;
        } catch (Exception ignored) {
        }
    }

    public static boolean hasHeadIcon(UUID uuid) {
        if (!isFiguraLoaded())
            return false;
        initReflection();
        if (!reflectionReady)
            return false;
        try {
            Object avatar = mGetAvatarForPlayer.invoke(null, uuid);
            if (avatar == null)
                return false;
            Object renderer = fRenderer.get(avatar);
            if (renderer == null)
                return false;
            Object tex = mGetTexture.invoke(renderer, "head_icon");
            if (tex == null)
                tex = mGetTexture.invoke(renderer, "head_icon.png");
            if (tex == null)
                return false;

            int w = (Integer) mGetWidth.invoke(tex);
            int h = (Integer) mGetHeight.invoke(tex);
            return w <= 32 && h <= 32;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static Component processMessage(Component message) {
        if (isFiguraLoaded()) {
            UUID uuid = ChatHeadMarker.extractFromSequence(message.getVisualOrderText());
            if (uuid != null && !hasHeadIcon(uuid)) {
                return ChatHeadMarker.stripMarker(message);
            }
            return message;
        }
        return ChatHeadMarker.stripMarker(message);
    }
}
