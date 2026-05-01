package com.io.storiosmod.particle.engine;

public class Particle {
    public double x, y, z;
    public double vx, vy, vz;
    public double size = 1.0;
    public float r = 1.0f, g = 1.0f, b = 1.0f, alpha = 1.0f;
    public double age = 0;
    public double lifetime = 2.0;
    public int index = 0;
    public boolean alive = true;

    public double life() {
        return lifetime > 0 ? Math.min(age / lifetime, 1.0) : 1.0;
    }

    public void fillContext(ExprContext ctx, double time, double dt, int totalCount) {
        ctx.set("t", time);
        ctx.set("dt", dt);
        ctx.set("i", index);
        ctx.set("count", totalCount);
        ctx.set("life", life());
        ctx.set("age", age);
        ctx.set("x", x);
        ctx.set("y", y);
        ctx.set("z", z);
        ctx.set("vx", vx);
        ctx.set("vy", vy);
        ctx.set("vz", vz);
        ctx.set("size", size);
        ctx.set("r", r);
        ctx.set("g", g);
        ctx.set("b", b);
        ctx.set("a", alpha);
    }

    public void reset() {
        x = y = z = 0;
        vx = vy = vz = 0;
        size = 1.0;
        r = g = b = alpha = 1.0f;
        age = 0;
        alive = true;
    }
}
