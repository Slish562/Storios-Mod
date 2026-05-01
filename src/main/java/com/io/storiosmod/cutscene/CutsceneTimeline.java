package com.io.storiosmod.cutscene;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CutsceneTimeline {
    private String name;
    private final List<CutscenePoint> points;
    private boolean loop;

    public CutsceneTimeline(String name) {
        this.name = name;
        this.points = new ArrayList<>();
        this.loop = false;
    }

    public CutsceneTimeline(String name, List<CutscenePoint> points, boolean loop) {
        this.name = name;
        this.points = new ArrayList<>(points);
        this.loop = loop;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<CutscenePoint> getPoints() { return points; }
    public boolean isLoop() { return loop; }
    public void setLoop(boolean loop) { this.loop = loop; }

    public void addPoint(CutscenePoint point) {
        points.add(point);
    }

    public void insertPoint(int index, CutscenePoint point) {
        points.add(Math.min(index, points.size()), point);
    }

    public void removePoint(int index) {
        if (index >= 0 && index < points.size()) {
            points.remove(index);
        }
    }

    public void movePointUp(int index) {
        if (index > 0 && index < points.size()) {
            CutscenePoint p = points.remove(index);
            points.add(index - 1, p);
        }
    }

    public void movePointDown(int index) {
        if (index >= 0 && index < points.size() - 1) {
            CutscenePoint p = points.remove(index);
            points.add(index + 1, p);
        }
    }

    public int getTotalDurationTicks() {
        int total = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            total += points.get(i).getDelayTicks() + points.get(i).getDurationTicks();
        }
        return total;
    }

    public Vec3 getPositionAt(int tick) {
        if (points.isEmpty()) return Vec3.ZERO;
        if (points.size() == 1) return points.get(0).getPos();

        int accumulated = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            int delay = points.get(i).getDelayTicks();
            if (tick < accumulated + delay) {
                return points.get(i).getPos();
            }
            accumulated += delay;
            int segDuration = points.get(i).getDurationTicks();
            if (tick <= accumulated + segDuration) {
                float t = segDuration > 0 ? (float)(tick - accumulated) / segDuration : 0;
                CutscenePoint current = points.get(i);
                CutscenePoint next = points.get(i + 1);
                Vec3 p0 = (i > 0) ? points.get(i - 1).getPos() : current.getPos();
                Vec3 p3 = (i + 2 < points.size()) ? points.get(i + 2).getPos() : next.getPos();
                return current.getInterpolation().interpolate(p0, current.getPos(), next.getPos(), p3, t);
            }
            accumulated += segDuration;
        }
        return points.get(points.size() - 1).getPos();
    }

    public float getYawAt(int tick) {
        return getAngleAt(tick, true);
    }

    public float getPitchAt(int tick) {
        return getAngleAt(tick, false);
    }

    private float getAngleAt(int tick, boolean isYaw) {
        if (points.isEmpty()) return 0;
        if (points.size() == 1) return isYaw ? points.get(0).getYaw() : points.get(0).getPitch();

        int accumulated = 0;
        for (int i = 0; i < points.size() - 1; i++) {
            int delay = points.get(i).getDelayTicks();
            if (tick < accumulated + delay) {
                return isYaw ? points.get(i).getYaw() : points.get(i).getPitch();
            }
            accumulated += delay;
            int segDuration = points.get(i).getDurationTicks();
            if (tick <= accumulated + segDuration) {
                float t = segDuration > 0 ? (float)(tick - accumulated) / segDuration : 0;
                CutscenePoint current = points.get(i);
                CutscenePoint next = points.get(i + 1);
                float a0 = (i > 0) ? getAngle(points.get(i - 1), isYaw) : getAngle(current, isYaw);
                float a3 = (i + 2 < points.size()) ? getAngle(points.get(i + 2), isYaw) : getAngle(next, isYaw);
                return current.getInterpolation().interpolateAngle(
                        a0, getAngle(current, isYaw), getAngle(next, isYaw), a3, t);
            }
            accumulated += segDuration;
        }
        CutscenePoint last = points.get(points.size() - 1);
        return isYaw ? last.getYaw() : last.getPitch();
    }

    private float getAngle(CutscenePoint p, boolean isYaw) {
        return isYaw ? p.getYaw() : p.getPitch();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name == null ? "" : name);
        buf.writeBoolean(loop);
        buf.writeVarInt(points.size());
        for (CutscenePoint point : points) {
            point.encode(buf);
        }
    }

    public static CutsceneTimeline decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        boolean loop = buf.readBoolean();
        int size = buf.readVarInt();
        List<CutscenePoint> points = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            points.add(CutscenePoint.decode(buf));
        }
        return new CutsceneTimeline(name, points, loop);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("name", name);
        json.addProperty("loop", loop);
        JsonArray arr = new JsonArray();
        for (CutscenePoint point : points) {
            arr.add(point.toJson());
        }
        json.add("points", arr);
        return json;
    }

    public static CutsceneTimeline fromJson(JsonObject json) {
        String name = json.get("name").getAsString();
        boolean loop = json.has("loop") && json.get("loop").getAsBoolean();
        JsonArray arr = json.getAsJsonArray("points");
        List<CutscenePoint> points = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            points.add(CutscenePoint.fromJson(arr.get(i).getAsJsonObject()));
        }
        return new CutsceneTimeline(name, points, loop);
    }
}

