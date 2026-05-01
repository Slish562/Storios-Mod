package com.io.storiosmod.client;

import com.io.storiosmod.cutscene.CutsceneTimeline;
import com.io.storiosmod.network.CutsceneFinishedPacket;
import com.io.storiosmod.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CutsceneCameraHandler {

    private static CutsceneTimeline activeTimeline;
    private static CutsceneTimeline previewTimeline;
    private static int currentTick;
    private static boolean playing;
    private static Vec3 cameraPos = Vec3.ZERO;
    private static float cameraYaw;
    private static float cameraPitch;
    private static float partialCameraYaw;
    private static float partialCameraPitch;
    private static Vec3 partialCameraPos = Vec3.ZERO;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(CutsceneCameraHandler.class);
    }

    public static void start(CutsceneTimeline timeline) {
        previewTimeline = null;
        activeTimeline = timeline;
        currentTick = 0;
        playing = true;
        updateCamera();
    }

    public static void startPreview(CutsceneTimeline timeline) {
        previewTimeline = timeline;
        activeTimeline = timeline;
        currentTick = 0;
        playing = true;
        updateCamera();
    }

    public static void stop() {
        playing = false;
        activeTimeline = null;
        currentTick = 0;

        PacketHandler.INSTANCE.sendToServer(new CutsceneFinishedPacket());

        if (previewTimeline != null) {
            CutsceneTimeline tl = previewTimeline;
            previewTimeline = null;
            Minecraft.getInstance().tell(() -> 
                Minecraft.getInstance().setScreen(new CutsceneEditorScreen(tl))
            );
        }
    }

    public static boolean isPlaying() {
        return playing && activeTimeline != null;
    }

    public static Vec3 getCameraPos() {
        return partialCameraPos;
    }

    public static float getCameraYaw() {
        return partialCameraYaw;
    }

    public static float getCameraPitch() {
        return partialCameraPitch;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isPlaying()) return;

        currentTick++;
        int totalDuration = activeTimeline.getTotalDurationTicks();
        if (currentTick >= totalDuration) {
            if (activeTimeline.isLoop()) {
                currentTick = 0;
            } else {
                stop();
                return;
            }
        }
        updateCamera();
    }

    private static void updateCamera() {
        if (activeTimeline == null) return;
        cameraPos = activeTimeline.getPositionAt(currentTick);
        cameraYaw = activeTimeline.getYawAt(currentTick);
        cameraPitch = activeTimeline.getPitchAt(currentTick);
        partialCameraPos = cameraPos;
        partialCameraYaw = cameraYaw;
        partialCameraPitch = cameraPitch;
    }

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (!isPlaying()) return;

        float partial = (float) event.getPartialTick();
        int nextTick = Math.min(currentTick + 1, activeTimeline.getTotalDurationTicks());

        Vec3 posNow = activeTimeline.getPositionAt(currentTick);
        Vec3 posNext = activeTimeline.getPositionAt(nextTick);
        partialCameraPos = posNow.lerp(posNext, partial);

        float yawNow = activeTimeline.getYawAt(currentTick);
        float yawNext = activeTimeline.getYawAt(nextTick);
        partialCameraYaw = lerpAngle(yawNow, yawNext, partial);

        float pitchNow = activeTimeline.getPitchAt(currentTick);
        float pitchNext = activeTimeline.getPitchAt(nextTick);
        partialCameraPitch = lerpAngle(pitchNow, pitchNext, partial);

        event.setYaw(partialCameraYaw);
        event.setPitch(partialCameraPitch);
        event.setRoll(0);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (isPlaying()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (isPlaying()) {
            event.setCanceled(true);
        }
    }

    private static float lerpAngle(float a, float b, float t) {
        float diff = ((b - a) % 360 + 540) % 360 - 180;
        return a + diff * t;
    }

    public static CutsceneTimeline getActiveTimeline() {
        return activeTimeline;
    }

    public static int getCurrentTick() {
        return currentTick;
    }
}
