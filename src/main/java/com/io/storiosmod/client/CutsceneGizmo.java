package com.io.storiosmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class CutsceneGizmo {

    public enum Axis { NONE, X, Y, Z }

    public static final float ARROW_LENGTH = 1.8f;
    public static final float PICK_THRESHOLD = 0.18f;

    public static Vec3 screenToWorldRay(double mouseX, double mouseY) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        float ndcX = (float) (2.0 * mouseX / w - 1.0);
        float ndcY = (float) (1.0 - 2.0 * mouseY / h);

        double fov = Math.toRadians(mc.options.fov().get());
        float aspect = (float) w / h;
        float tanHalfFov = (float) Math.tan(fov / 2.0);

        float camPitch = mc.gameRenderer.getMainCamera().getXRot();
        float camYaw = mc.gameRenderer.getMainCamera().getYRot();

        double pitchRad = Math.toRadians(camPitch);
        double yawRad = Math.toRadians(camYaw);

        double fx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double fy = -Math.sin(pitchRad);
        double fz = Math.cos(yawRad) * Math.cos(pitchRad);

        double rx = -Math.cos(yawRad);
        double ry = 0;
        double rz = -Math.sin(yawRad);

        double ux = -Math.sin(yawRad) * Math.sin(pitchRad);
        double uy = Math.cos(pitchRad);
        double uz = Math.cos(yawRad) * Math.sin(pitchRad);

        double dirX = fx + ndcX * aspect * tanHalfFov * rx + ndcY * tanHalfFov * ux;
        double dirY = fy + ndcX * aspect * tanHalfFov * ry + ndcY * tanHalfFov * uy;
        double dirZ = fz + ndcX * aspect * tanHalfFov * rz + ndcY * tanHalfFov * uz;

        double len = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        return new Vec3(dirX / len, dirY / len, dirZ / len);
    }

    public static Axis pickAxis(double mouseX, double mouseY, Vec3 pointPos) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 rayDir = screenToWorldRay(mouseX, mouseY);

        float distX = rayToSegmentDistance(camPos, rayDir, pointPos, new Vec3(1, 0, 0), ARROW_LENGTH);
        float distY = rayToSegmentDistance(camPos, rayDir, pointPos, new Vec3(0, 1, 0), ARROW_LENGTH);
        float distZ = rayToSegmentDistance(camPos, rayDir, pointPos, new Vec3(0, 0, 1), ARROW_LENGTH);

        float distToCam = (float) camPos.distanceTo(pointPos);
        float minDist = Math.max(PICK_THRESHOLD, distToCam * 0.04f);

        Axis picked = Axis.NONE;

        if (distX < minDist) { minDist = distX; picked = Axis.X; }
        if (distY < minDist) { minDist = distY; picked = Axis.Y; }
        if (distZ < minDist) { minDist = distZ; picked = Axis.Z; }

        return picked;
    }

    public static float rayToSegmentDistance(Vec3 rayOrigin, Vec3 rayDir, Vec3 segStart, Vec3 segDir, float segLength) {
        Vec3 w0 = rayOrigin.subtract(segStart);
        double a = rayDir.dot(rayDir);
        double b = rayDir.dot(segDir);
        double c = segDir.dot(segDir);
        double d = rayDir.dot(w0);
        double e = segDir.dot(w0);
        double denom = a * c - b * b;

        double sc, tc;
        if (denom < 1e-8) {
            sc = 0;
            tc = e / c;
        } else {
            sc = (b * e - c * d) / denom;
            tc = (a * e - b * d) / denom;
        }

        sc = Math.max(0, sc);
        tc = Math.max(0, Math.min(tc, segLength));

        Vec3 closestOnRay = rayOrigin.add(rayDir.scale(sc));
        Vec3 closestOnSeg = segStart.add(segDir.scale(tc));

        return (float) closestOnRay.distanceTo(closestOnSeg);
    }

    public static double rayLineClosestParam(Vec3 rayOrigin, Vec3 rayDir, Vec3 lineOrigin, Vec3 lineDir) {
        Vec3 w0 = rayOrigin.subtract(lineOrigin);
        double a = rayDir.dot(rayDir);
        double b = rayDir.dot(lineDir);
        double c = lineDir.dot(lineDir);
        double d = rayDir.dot(w0);
        double e = lineDir.dot(w0);
        double denom = a * c - b * b;
        if (denom < 1e-8) return 0;
        return (a * e - b * d) / denom;
    }

    public static Vec3 getAxisDir(Axis axis) {
        return switch (axis) {
            case X -> new Vec3(1, 0, 0);
            case Y -> new Vec3(0, 1, 0);
            case Z -> new Vec3(0, 0, 1);
            default -> Vec3.ZERO;
        };
    }

    public static int pickPoint(double mouseX, double mouseY, java.util.List<? extends Vec3Provider> points) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 rayDir = screenToWorldRay(mouseX, mouseY);

        int best = -1;
        double bestDist = Double.MAX_VALUE;

        for (int i = 0; i < points.size(); i++) {
            Vec3 pos = points.get(i).getPos();
            Vec3 toPoint = pos.subtract(camPos);
            double t = toPoint.dot(rayDir);
            if (t < 0) continue;
            Vec3 closest = camPos.add(rayDir.scale(t));
            double dist = closest.distanceTo(pos);
            double distToCam = camPos.distanceTo(pos);
            double threshold = Math.max(0.5, distToCam * 0.05);
            if (dist < threshold && dist < bestDist) {
                bestDist = dist;
                best = i;
            }
        }
        return best;
    }

    public interface Vec3Provider {
        Vec3 getPos();
    }
}

