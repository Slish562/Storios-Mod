package com.io.storiosmod.mixin;

import com.io.storiosmod.chat.ChatHeadMarker;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ChatComponent.class)
public abstract class ChatComponentHeadMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private List<GuiMessage.Line> trimmedMessages;
    @Shadow
    private int chatScrollbarPos;

    @Unique
    private static final int STORIOS_HEAD_SIZE = 8;
    @Unique
    private static final String STORIOS_HEAD_TEXTURE_NAME = "head_icon";

    @Inject(method = "render", at = @At("TAIL"))
    private void storiosmod$renderChatHeads(GuiGraphics guiGraphics, int tickCount, int mouseX, int mouseY, boolean focused,
            CallbackInfo ci) {
        if (trimmedMessages.isEmpty())
            return;
        if (minecraft.getConnection() == null)
            return;
        if (!net.neoforged.fml.ModList.get().isLoaded("figura"))
            return;

        int linesPerPage = storiosmod$getLinesPerPage();
        int size = trimmedMessages.size();
        float scale = minecraft.options.chatScale().get().floatValue();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int baseY = Mth.floor((float) (screenHeight - 40) / scale);

        int chatTop = Mth.floor((float) (screenHeight - 40) / scale) - linesPerPage * 9;
        int scaledChatTop = Mth.floor(chatTop * scale);
        int scaledChatBottom = screenHeight - 40;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(4.0, 0.0, 100.0);

        guiGraphics.enableScissor(0, scaledChatTop, minecraft.getWindow().getGuiScaledWidth(), scaledChatBottom);

        for (int i = 0; i + chatScrollbarPos < size && i < linesPerPage; i++) {
            GuiMessage.Line line = trimmedMessages.get(i + chatScrollbarPos);
            if (line == null)
                continue;

            int ticksSince = tickCount - line.addedTime();
            if (ticksSince >= 200 && !storiosmod$isFocused())
                continue;

            UUID uuid = ChatHeadMarker.extractFromSequence(line.content());
            if (uuid == null)
                continue;

            int[] headData = storiosmod$getHeadIcon(uuid);
            if (headData == null)
                continue;
            int texW = headData[1];
            int texH = headData[2];
            ResourceLocation headTexture = storiosmod$headLocationCache;
            if (headTexture == null)
                continue;

            double opacity = storiosmod$isFocused() ? 1.0 : storiosmod$getTimeFactor(ticksSince);
            int alpha = (int) (255.0 * opacity * minecraft.options.chatOpacity().get());
            if (alpha <= 3)
                continue;

            int y = baseY - i * 9;

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha / 255.0F);
            guiGraphics.blit(headTexture, -2, y - 9, STORIOS_HEAD_SIZE, STORIOS_HEAD_SIZE, 0.0F, 0.0F, texW, texH, texW,
                    texH);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();
    }

    @Unique
    private ResourceLocation storiosmod$headLocationCache;

    @Unique
    private static boolean storiosmod$reflectionInitialized = false;
    @Unique
    private static java.lang.reflect.Method storiosmod$mGetAvatarForPlayer;
    @Unique
    private static java.lang.reflect.Field storiosmod$fRenderer;
    @Unique
    private static java.lang.reflect.Method storiosmod$mGetTexture;
    @Unique
    private static java.lang.reflect.Method storiosmod$mUploadIfDirty;
    @Unique
    private static java.lang.reflect.Method storiosmod$mGetLocation;
    @Unique
    private static java.lang.reflect.Method storiosmod$mGetWidth;
    @Unique
    private static java.lang.reflect.Method storiosmod$mGetHeight;

    @Unique
    private void storiosmod$initReflection() {
        if (storiosmod$reflectionInitialized)
            return;
        storiosmod$reflectionInitialized = true;
        try {
            Class<?> avatarManagerClass = Class.forName("org.figuramc.figura.avatar.AvatarManager");
            storiosmod$mGetAvatarForPlayer = avatarManagerClass.getMethod("getAvatarForPlayer", UUID.class);

            Class<?> avatarClass = Class.forName("org.figuramc.figura.avatar.Avatar");
            storiosmod$fRenderer = avatarClass.getField("renderer");

            Class<?> avatarRendererClass = Class.forName("org.figuramc.figura.model.rendering.AvatarRenderer");
            storiosmod$mGetTexture = avatarRendererClass.getMethod("getTexture", String.class);

            Class<?> figuraTextureClass = Class.forName("org.figuramc.figura.model.rendering.texture.FiguraTexture");
            storiosmod$mUploadIfDirty = figuraTextureClass.getMethod("uploadIfDirty");
            storiosmod$mGetLocation = figuraTextureClass.getMethod("getLocation");
            storiosmod$mGetWidth = figuraTextureClass.getMethod("getWidth");
            storiosmod$mGetHeight = figuraTextureClass.getMethod("getHeight");
        } catch (Exception ignored) {
        }
    }

    @Unique
    private int[] storiosmod$getHeadIcon(UUID playerUuid) {
        storiosmod$headLocationCache = null;
        storiosmod$initReflection();
        if (storiosmod$mGetAvatarForPlayer == null)
            return null;

        try {
            Object avatar = storiosmod$mGetAvatarForPlayer.invoke(null, playerUuid);
            if (avatar == null)
                return null;

            Object renderer = storiosmod$fRenderer.get(avatar);
            if (renderer == null)
                return null;

            Object figuraTexture = storiosmod$mGetTexture.invoke(renderer, STORIOS_HEAD_TEXTURE_NAME);
            if (figuraTexture == null) {
                figuraTexture = storiosmod$mGetTexture.invoke(renderer, STORIOS_HEAD_TEXTURE_NAME + ".png");
            }
            if (figuraTexture == null)
                return null;

            storiosmod$mUploadIfDirty.invoke(figuraTexture);
            storiosmod$headLocationCache = (ResourceLocation) storiosmod$mGetLocation.invoke(figuraTexture);

            int w = (Integer) storiosmod$mGetWidth.invoke(figuraTexture);
            int h = (Integer) storiosmod$mGetHeight.invoke(figuraTexture);

            if (w > 32 || h > 32)
                return null;

            return new int[] { 0, w, h };
        } catch (Exception ignored) {
            return null;
        }
    }

    @Unique
    private boolean storiosmod$isFocused() {
        return minecraft.screen instanceof net.minecraft.client.gui.screens.ChatScreen;
    }

    @Unique
    private int storiosmod$getLinesPerPage() {
        float scale = minecraft.options.chatScale().get().floatValue();
        int height = Mth.floor((float) (minecraft.getWindow().getGuiScaledHeight() - 40) / scale);
        return height / 9;
    }

    @Unique
    private static double storiosmod$getTimeFactor(int ticks) {
        double d = (double) ticks / 200.0;
        d = 1.0 - d;
        d *= 10.0;
        d = Mth.clamp(d, 0.0, 1.0);
        d *= d;
        return d;
    }
}

