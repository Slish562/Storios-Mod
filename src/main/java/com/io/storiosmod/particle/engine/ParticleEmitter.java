package com.io.storiosmod.particle.engine;

import java.util.ArrayList;
import java.util.List;

public class ParticleEmitter {

    @FunctionalInterface
    public interface PositionSupplier {
        double[] get();
    }

    private int maxParticles = 100;
    private double spawnRate = 10;
    private double lifetime = 2.0;
    private boolean looping = true;
    private boolean worldSpace = false;

    Expression posX = Expression.constant(0);
    Expression posY = Expression.constant(0);
    Expression posZ = Expression.constant(0);
    Expression velX = Expression.constant(0);
    Expression velY = Expression.constant(0);
    Expression velZ = Expression.constant(0);
    Expression sizeExpr = Expression.constant(1);
    Expression alphaExpr = Expression.constant(1);
    Expression colorR = Expression.constant(1);
    Expression colorG = Expression.constant(1);
    Expression colorB = Expression.constant(1);

    Shapes.SpawnFunction spawnShape = Shapes.point();

    private double originX, originY, originZ;
    private PositionSupplier followTarget;

    private final List<Particle> particles = new ArrayList<>();
    private final ExprContext ctx = new ExprContext();
    private double time = 0;
    private double spawnAccumulator = 0;
    private boolean active = true;

    public ParticleEmitter maxParticles(int n) {
        this.maxParticles = n;
        return this;
    }

    public ParticleEmitter spawnRate(double rate) {
        this.spawnRate = rate;
        return this;
    }

    public ParticleEmitter lifetime(double lt) {
        this.lifetime = lt;
        return this;
    }

    public ParticleEmitter looping(boolean l) {
        this.looping = l;
        return this;
    }

    public ParticleEmitter worldSpace(boolean ws) {
        this.worldSpace = ws;
        return this;
    }

    public ParticleEmitter origin(double x, double y, double z) {
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        return this;
    }

    public ParticleEmitter follow(PositionSupplier target) {
        this.followTarget = target;
        return this;
    }

    public void update(double dt) {
        if (!active)
            return;

        time += dt;

        if (followTarget != null) {
            double[] pos = followTarget.get();
            originX = pos[0];
            originY = pos[1];
            originZ = pos[2];
        }

        if (looping || time < 0.1) {
            spawnAccumulator += spawnRate * dt;
            while (spawnAccumulator >= 1.0 && particles.size() < maxParticles) {
                spawnAccumulator -= 1.0;
                spawnParticle();
            }
        }

        int alive = particles.size();
        for (int idx = particles.size() - 1; idx >= 0; idx--) {
            Particle p = particles.get(idx);
            p.age += dt;

            if (p.age >= p.lifetime) {
                p.alive = false;
                particles.remove(idx);
                continue;
            }

            p.fillContext(ctx, time, dt, alive);

            double fx = posX.eval(ctx);
            double fy = posY.eval(ctx);
            double fz = posZ.eval(ctx);

            if (worldSpace) {
                p.x = fx;
                p.y = fy;
                p.z = fz;
            } else {
                p.vx = velX.eval(ctx);
                p.vy = velY.eval(ctx);
                p.vz = velZ.eval(ctx);
                p.x = originX + fx + p.vx * p.age;
                p.y = originY + fy + p.vy * p.age;
                p.z = originZ + fz + p.vz * p.age;
            }

            p.size = sizeExpr.eval(ctx);
            p.alpha = (float) Math.max(0, Math.min(1, alphaExpr.eval(ctx)));
            p.r = (float) Math.max(0, Math.min(1, colorR.eval(ctx)));
            p.g = (float) Math.max(0, Math.min(1, colorG.eval(ctx)));
            p.b = (float) Math.max(0, Math.min(1, colorB.eval(ctx)));
        }
    }

    private void spawnParticle() {
        Particle p = new Particle();
        p.index = particles.size();
        p.lifetime = this.lifetime;
        spawnShape.apply(p, p.index, maxParticles, time);
        p.x += originX;
        p.y += originY;
        p.z += originZ;
        particles.add(p);
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public double getTime() {
        return time;
    }

    public boolean isActive() {
        return active;
    }

    public void start() {
        active = true;
    }

    public void stop() {
        active = false;
    }

    public void reset() {
        particles.clear();
        time = 0;
        spawnAccumulator = 0;
        active = true;
    }
}
