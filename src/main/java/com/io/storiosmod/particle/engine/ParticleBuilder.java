package com.io.storiosmod.particle.engine;

public class ParticleBuilder {

    private final ParticleEmitter emitter = new ParticleEmitter();

    public static ParticleBuilder create(String posX, String posY, String posZ) {
        ParticleBuilder b = new ParticleBuilder();
        b.emitter.posX = Expression.parse(posX);
        b.emitter.posY = Expression.parse(posY);
        b.emitter.posZ = Expression.parse(posZ);
        return b;
    }

    public static ParticleBuilder create(String posX, String posY) {
        return create(posX, posY, "0");
    }

    public static ParticleBuilder burst(String velX, String velY, String velZ) {
        ParticleBuilder b = new ParticleBuilder();
        b.emitter.velX = Expression.parse(velX);
        b.emitter.velY = Expression.parse(velY);
        b.emitter.velZ = Expression.parse(velZ);
        b.emitter.looping(false);
        return b;
    }

    public static ParticleBuilder shape(Shapes.SpawnFunction shape) {
        ParticleBuilder b = new ParticleBuilder();
        b.emitter.spawnShape = shape;
        return b;
    }

    public static ParticleBuilder trail(ParticleEmitter.PositionSupplier target) {
        ParticleBuilder b = new ParticleBuilder();
        b.emitter.follow(target);
        b.emitter.spawnRate(20);
        b.emitter.posX = Expression.parse("random(-0.3, 0.3)");
        b.emitter.posY = Expression.parse("random(-0.3, 0.3)");
        b.emitter.posZ = Expression.parse("random(-0.3, 0.3)");
        b.emitter.alphaExpr = Expression.parse("1 - life");
        return b;
    }

    public ParticleBuilder count(int n) {
        emitter.maxParticles(n);
        emitter.spawnRate(n * 2);
        return this;
    }

    public ParticleBuilder rate(double perSecond) {
        emitter.spawnRate(perSecond);
        return this;
    }

    public ParticleBuilder lifetime(double seconds) {
        emitter.lifetime(seconds);
        return this;
    }

    public ParticleBuilder loop(boolean looping) {
        emitter.looping(looping);
        return this;
    }

    public ParticleBuilder position(String x, String y, String z) {
        emitter.posX = Expression.parse(x);
        emitter.posY = Expression.parse(y);
        emitter.posZ = Expression.parse(z);
        return this;
    }

    public ParticleBuilder velocity(String vx, String vy, String vz) {
        emitter.velX = Expression.parse(vx);
        emitter.velY = Expression.parse(vy);
        emitter.velZ = Expression.parse(vz);
        return this;
    }

    public ParticleBuilder size(String formula) {
        emitter.sizeExpr = Expression.parse(formula);
        return this;
    }

    public ParticleBuilder size(double value) {
        emitter.sizeExpr = Expression.constant(value);
        return this;
    }

    public ParticleBuilder alpha(String formula) {
        emitter.alphaExpr = Expression.parse(formula);
        return this;
    }

    public ParticleBuilder alpha(double value) {
        emitter.alphaExpr = Expression.constant(value);
        return this;
    }

    public ParticleBuilder color(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255f;
        float g = ((hex >> 8) & 0xFF) / 255f;
        float b = (hex & 0xFF) / 255f;
        emitter.colorR = Expression.constant(r);
        emitter.colorG = Expression.constant(g);
        emitter.colorB = Expression.constant(b);
        return this;
    }

    public ParticleBuilder color(String r, String g, String b) {
        emitter.colorR = Expression.parse(r);
        emitter.colorG = Expression.parse(g);
        emitter.colorB = Expression.parse(b);
        return this;
    }

    public ParticleBuilder at(double x, double y, double z) {
        emitter.origin(x, y, z);
        return this;
    }

    public ParticleBuilder follow(ParticleEmitter.PositionSupplier target) {
        emitter.follow(target);
        return this;
    }

    public ParticleBuilder withShape(Shapes.SpawnFunction shape) {
        emitter.spawnShape = shape;
        return this;
    }

    public ParticleEmitter build() {
        return emitter;
    }
}

