package com.io.storiosmod.cutscene;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

public class CutscenePoint implements com.io.storiosmod.client.CutsceneGizmo.Vec3Provider {
    private double x, y, z;
    private float yaw, pitch;
    private InterpolationType interpolation;
    private int durationTicks;
    private float speed;
    private int delayTicks;

    public CutscenePoint(double x, double y, double z, float yaw, float pitch,
                         InterpolationType interpolation, int durationTicks) {
        this(x, y, z, yaw, pitch, interpolation, durationTicks, 0f, 0);
    }

    public CutscenePoint(double x, double y, double z, float yaw, float pitch,
                         InterpolationType interpolation, int durationTicks,
                         float speed, int delayTicks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.interpolation = interpolation;
        this.durationTicks = durationTicks;
        this.speed = speed;
        this.delayTicks = delayTicks;
    }

    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public InterpolationType getInterpolation() { return interpolation; }
    public int getDurationTicks() { return durationTicks; }
    public float getSpeed() { return speed; }
    public int getDelayTicks() { return delayTicks; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }
    public void setInterpolation(InterpolationType interpolation) { this.interpolation = interpolation; }
    public void setDurationTicks(int durationTicks) { this.durationTicks = durationTicks; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setDelayTicks(int delayTicks) { this.delayTicks = delayTicks; }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeEnum(interpolation);
        buf.writeVarInt(durationTicks);
        buf.writeFloat(speed);
        buf.writeVarInt(delayTicks);
    }

    public static CutscenePoint decode(FriendlyByteBuf buf) {
        return new CutscenePoint(
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readEnum(InterpolationType.class),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readVarInt()
        );
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("x", x);
        json.addProperty("y", y);
        json.addProperty("z", z);
        json.addProperty("yaw", yaw);
        json.addProperty("pitch", pitch);
        json.addProperty("interpolation", interpolation.name());
        json.addProperty("duration", durationTicks);
        json.addProperty("speed", speed);
        json.addProperty("delay", delayTicks);
        return json;
    }

    public static CutscenePoint fromJson(JsonObject json) {
        return new CutscenePoint(
                json.get("x").getAsDouble(),
                json.get("y").getAsDouble(),
                json.get("z").getAsDouble(),
                json.get("yaw").getAsFloat(),
                json.get("pitch").getAsFloat(),
                InterpolationType.valueOf(json.get("interpolation").getAsString()),
                json.get("duration").getAsInt(),
                json.has("speed") ? json.get("speed").getAsFloat() : 0f,
                json.has("delay") ? json.get("delay").getAsInt() : 0
        );
    }

    public CutscenePoint copy() {
        return new CutscenePoint(x, y, z, yaw, pitch, interpolation, durationTicks, speed, delayTicks);
    }
}
