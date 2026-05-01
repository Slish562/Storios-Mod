package com.io.storiosmod.client;

import com.io.storiosmod.cutscene.CutscenePoint;
import com.io.storiosmod.cutscene.CutsceneTimeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.SubscribeEvent;
import org.joml.Matrix4f;

import java.util.List;

public class CutscenePathRenderer {

    private static CutsceneTimeline editorTimeline;
    private static int selectedIndex = -1;

    public static void register() {
        NeoForge.EVENT_BUS.register(CutscenePathRenderer.class);
    }

    public static void setEditorTimeline(CutsceneTimeline timeline) {
        editorTimeline = timeline;
    }

    public static void clearEditorTimeline() {
        editorTimeline = null;
        selectedIndex = -1;
    }

    public static void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    private static CutsceneGizmo.Axis hoveredAxis = CutsceneGizmo.Axis.NONE;

    public static void setHoveredAxis(CutsceneGizmo.Axis axis) {
        hoveredAxis = axis;
    }

    public static CutsceneGizmo.Axis getHoveredAxis() {
        return hoveredAxis;
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (editorTimeline == null) return;

        List<CutscenePoint> points = editorTimeline.getPoints();
        if (points.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull(); 
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < points.size(); i++) {
            CutscenePoint p = points.get(i);
            boolean isSelected = (i == selectedIndex);
            renderPoint(matrix, p, isSelected);
            renderDirectionArrow(matrix, p, isSelected);
        }

        if (selectedIndex >= 0 && selectedIndex < points.size()) {
            CutscenePoint sel = points.get(selectedIndex);
            renderGizmo(matrix, sel);
        }

        if (points.size() >= 2) {
            renderPath(matrix, editorTimeline);
        }

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void renderPoint(Matrix4f matrix, CutscenePoint point, boolean selected) {
        float size = selected ? 0.25f : 0.15f;
        float r = selected ? 1.0f : 0.2f;
        float g = selected ? 0.85f : 0.8f;
        float b = selected ? 0.0f : 1.0f;
        float a = 0.85f;
        float x = (float) point.getX();
        float y = (float) point.getY();
        float z = (float) point.getZ();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buf.addVertex(matrix, x - size, y - size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y - size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y + size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y + size, z - size).setColor(r, g, b, a);

        buf.addVertex(matrix, x - size, y - size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y - size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y + size, z + size).setColor(r, g, b, a);

        buf.addVertex(matrix, x - size, y + size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y + size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y + size, z + size).setColor(r, g, b, a);

        buf.addVertex(matrix, x - size, y - size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y - size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y - size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y - size, z + size).setColor(r, g, b, a);

        buf.addVertex(matrix, x - size, y - size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y + size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y + size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x - size, y - size, z + size).setColor(r, g, b, a);

        buf.addVertex(matrix, x + size, y - size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y + size, z - size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y + size, z + size).setColor(r, g, b, a);
        buf.addVertex(matrix, x + size, y - size, z + size).setColor(r, g, b, a);

        net.minecraft.client.renderer.GameRenderer.getPositionColorShader();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static void renderDirectionArrow(Matrix4f matrix, CutscenePoint point, boolean selected) {
        float yawRad = (float) Math.toRadians(-point.getYaw());
        float pitchRad = (float) Math.toRadians(-point.getPitch());

        float dirX = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        float dirY = (float) Math.sin(pitchRad);
        float dirZ = (float) (Math.cos(yawRad) * Math.cos(pitchRad));

        float len = 1.2f;
        float x = (float) point.getX();
        float y = (float) point.getY();
        float z = (float) point.getZ();

        float endX = x + dirX * len;
        float endY = y + dirY * len;
        float endZ = z + dirZ * len;

        float r = selected ? 1.0f : 0.9f;
        float g = selected ? 0.5f : 0.4f;
        float b = 0.1f;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buf.addVertex(matrix, x, y, z).setColor(r, g, b, 1.0f);
        buf.addVertex(matrix, endX, endY, endZ).setColor(r, g, b, 1.0f);
        net.minecraft.client.renderer.GameRenderer.getPositionColorShader();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static void renderPath(Matrix4f matrix, CutsceneTimeline timeline) {
        int totalDuration = timeline.getTotalDurationTicks();
        if (totalDuration <= 0) return;

        int steps = Math.max(totalDuration * 2, 100);

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < steps; i++) {
            int tick1 = (int) ((float) i / steps * totalDuration);
            int tick2 = (int) ((float) (i + 1) / steps * totalDuration);
            Vec3 pos1 = timeline.getPositionAt(tick1);
            Vec3 pos2 = timeline.getPositionAt(tick2);

            float t = (float) i / steps;
            float r = 0.3f + 0.7f * t;
            float g = 1.0f - 0.5f * t;
            float b = 0.4f;

            buf.addVertex(matrix, (float) pos1.x, (float) pos1.y, (float) pos1.z).setColor(r, g, b, 0.9f);
            buf.addVertex(matrix, (float) pos2.x, (float) pos2.y, (float) pos2.z).setColor(r, g, b, 0.9f);
        }

        net.minecraft.client.renderer.GameRenderer.getPositionColorShader();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static void renderGizmo(Matrix4f matrix, CutscenePoint point) {
        float x = (float) point.getX();
        float y = (float) point.getY();
        float z = (float) point.getZ();
        float len = CutsceneGizmo.ARROW_LENGTH;

        renderGizmoArrow(matrix, x, y, z, len, 0, 0, 1.0f, 0.2f, 0.2f,
                hoveredAxis == CutsceneGizmo.Axis.X);
        renderGizmoArrow(matrix, x, y, z, 0, len, 0, 0.2f, 1.0f, 0.2f,
                hoveredAxis == CutsceneGizmo.Axis.Y);
        renderGizmoArrow(matrix, x, y, z, 0, 0, len, 0.3f, 0.3f, 1.0f,
                hoveredAxis == CutsceneGizmo.Axis.Z);
    }

    private static void renderGizmoArrow(Matrix4f matrix, float ox, float oy, float oz,
                                          float dx, float dy, float dz,
                                          float r, float g, float b, boolean hovered) {
        float alpha = hovered ? 1.0f : 0.8f;
        float thickness = hovered ? 0.06f : 0.035f;
        if (hovered) { r = Math.min(1.0f, r + 0.3f); g = Math.min(1.0f, g + 0.3f); b = Math.min(1.0f, b + 0.3f); }

        float ex = ox + dx;
        float ey = oy + dy;
        float ez = oz + dz;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf;

        buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        if (Math.abs(dx) > 0.001f) {

            buf.addVertex(matrix, ox, oy - thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy - thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy - thickness, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox, oy - thickness, oz + thickness).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox, oy + thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy + thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy + thickness, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox, oy + thickness, oz + thickness).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox, oy - thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy - thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy + thickness, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox, oy + thickness, oz - thickness).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox, oy - thickness, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy - thickness, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, oy + thickness, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox, oy + thickness, oz + thickness).setColor(r, g, b, alpha);
        } else if (Math.abs(dy) > 0.001f) {

            buf.addVertex(matrix, ox - thickness, oy, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, ey, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, ey, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, oy, oz + thickness).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox + thickness, oy, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, ey, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, ey, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy, oz + thickness).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox - thickness, oy, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, ey, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, ey, oz - thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy, oz - thickness).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox - thickness, oy, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, ey, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, ey, oz + thickness).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy, oz + thickness).setColor(r, g, b, alpha);
        } else {

            buf.addVertex(matrix, ox - thickness, oy - thickness, oz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, oy - thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, oy + thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, oy + thickness, oz).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox + thickness, oy - thickness, oz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy - thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy + thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy + thickness, oz).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox - thickness, oy - thickness, oz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, oy - thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy - thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy - thickness, oz).setColor(r, g, b, alpha);

            buf.addVertex(matrix, ox - thickness, oy + thickness, oz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox - thickness, oy + thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy + thickness, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ox + thickness, oy + thickness, oz).setColor(r, g, b, alpha);
        }

        net.minecraft.client.renderer.GameRenderer.getPositionColorShader();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());

        float tipLen = 0.25f;
        float tipR = hovered ? 0.12f : 0.08f;
        float ndx = dx == 0 ? 0 : dx / Math.abs(dx);
        float ndy = dy == 0 ? 0 : dy / Math.abs(dy);
        float ndz = dz == 0 ? 0 : dz / Math.abs(dz);
        float tx = ex + ndx * tipLen;
        float ty = ey + ndy * tipLen;
        float tz = ez + ndz * tipLen;

        buf = tess.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);

        if (Math.abs(dx) > 0.001f) {

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey - tipR, ez - tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey + tipR, ez - tipR).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey + tipR, ez - tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey + tipR, ez + tipR).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey + tipR, ez + tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey - tipR, ez + tipR).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey - tipR, ez + tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex, ey - tipR, ez - tipR).setColor(r, g, b, alpha);
        } else if (Math.abs(dy) > 0.001f) {

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey, ez - tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey, ez - tipR).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey, ez - tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey, ez + tipR).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey, ez + tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey, ez + tipR).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey, ez + tipR).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey, ez - tipR).setColor(r, g, b, alpha);
        } else {

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey - tipR, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey - tipR, ez).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey - tipR, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey + tipR, ez).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex + tipR, ey + tipR, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey + tipR, ez).setColor(r, g, b, alpha);

            buf.addVertex(matrix, tx, ty, tz).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey + tipR, ez).setColor(r, g, b, alpha);
            buf.addVertex(matrix, ex - tipR, ey - tipR, ez).setColor(r, g, b, alpha);
        }

        net.minecraft.client.renderer.GameRenderer.getPositionColorShader();
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorShader);
        com.mojang.blaze3d.vertex.BufferUploader.drawWithShader(buf.buildOrThrow());
    }
}


