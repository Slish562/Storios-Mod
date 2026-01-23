package com.io.storiosmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientTitleHandler {

    private static Component currentMessage;
    private static String vAnchor;
    private static int vOffset;
    private static String hAnchor;
    private static int hOffset;
    private static float scale;

    private static int fadeInTicks;
    private static int stayTicks;
    private static int fadeOutTicks;
    private static int displayTicks;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(ClientTitleHandler.class);
    }

    public static void setCustomTitle(Component message, String vA, int vO, String hA, int hO, float s, int fadeIn,
            int stay, int fadeOut) {
        currentMessage = message;
        vAnchor = vA;
        vOffset = vO;
        hAnchor = hA;
        hOffset = hO;
        scale = s;
        fadeInTicks = fadeIn;
        stayTicks = stay;
        fadeOutTicks = fadeOut;
        displayTicks = fadeIn + stay + fadeOut;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && displayTicks > 0) {
            displayTicks--;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type() && displayTicks > 0 && currentMessage != null) {
            renderTitle(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(),
                    event.getWindow().getGuiScaledHeight());
        }
    }

    private static void renderTitle(GuiGraphics guiGraphics, int screenWidth, int screenHeight) {
        if (currentMessage == null)
            return;

        Minecraft mc = Minecraft.getInstance();
        guiGraphics.pose().pushPose();

        float opacity = 1.0f;
        int maxTicks = fadeInTicks + stayTicks + fadeOutTicks;
        int current = displayTicks;

        if (current > stayTicks + fadeOutTicks) { // Fading in
            opacity = (fadeInTicks > 0) ? (float) (maxTicks - current) / (float) fadeInTicks : 1.0f;
        } else if (current <= fadeOutTicks) { // Fading out
            opacity = (fadeOutTicks > 0) ? (float) current / (float) fadeOutTicks : 1.0f;
        }

        if (opacity < 0)
            opacity = 0;
        if (opacity > 1)
            opacity = 1;

        int alpha = (int) (opacity * 255) << 24;

        float textWidth = mc.font.width(currentMessage);

        // Calculate Position
        float x = 0;
        float y = 0;

        // Horizontal
        switch (hAnchor.toLowerCase()) {
            case "left":
                x = 0;
                break;
            case "center":
                x = screenWidth / 2.0f;
                break;
            case "right":
                x = screenWidth;
                break;
        }
        x += hOffset;

        // Vertical
        switch (vAnchor.toLowerCase()) {
            case "top":
                y = 0;
                break;
            case "center":
                y = screenHeight / 2.0f;
                break;
            case "bottom":
                y = screenHeight;
                break;
        }
        y -= vOffset;

        guiGraphics.pose().translate(x, y, 0);
        guiGraphics.pose().scale(scale, scale, 1.0f);

        float drawX = 0;
        float drawY = 0; // Default Top

        if (hAnchor.equalsIgnoreCase("center")) {
            drawX = -textWidth / 2.0f;
        } else if (hAnchor.equalsIgnoreCase("right")) {
            drawX = -textWidth;
        }
        // If Left, drawX is 0.

        if (vAnchor.equalsIgnoreCase("center")) {
            drawY = -mc.font.lineHeight / 2.0f;
        } else if (vAnchor.equalsIgnoreCase("bottom")) {
            drawY = -mc.font.lineHeight;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, opacity);

        guiGraphics.drawString(mc.font, currentMessage, (int) drawX, (int) drawY, 0xFFFFFF | alpha);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.pose().popPose();
    }
}
