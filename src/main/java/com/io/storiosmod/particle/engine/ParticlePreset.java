package com.io.storiosmod.particle.engine;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ParticlePreset {
    public String name = "unnamed";
    public String posX = "0", posY = "0", posZ = "0";
    public String velX = "0", velY = "0", velZ = "0";
    public String size = "1";
    public String alpha = "1";
    public String colorR = "1", colorG = "1", colorB = "1";
    public int count = 100;
    public double lifetime = 2.0;
    public double spawnRate = 10;
    public boolean looping = true;
    public String shapeName = "point";
    public double shapeParam1 = 0, shapeParam2 = 0, shapeParam3 = 0;

    public static final ParticlePreset VORTEX = new ParticlePreset() {
        {
            name = "vortex";
            posX = "cos(t * 3 + i / count * TAU) * (2 + sin(t) * 0.5)";
            posY = "i / count * 3";
            posZ = "sin(t * 3 + i / count * TAU) * (2 + sin(t) * 0.5)";
            size = "0.3 * (1 - life)";
            alpha = "1 - life";
            colorR = "0.2";
            colorG = "0.6";
            colorB = "1.0";
            count = 60;
            lifetime = 2.0;
        }
    };

    public static final ParticlePreset FIREWORK = new ParticlePreset() {
        {
            name = "firework";
            posX = "random(-1, 1)";
            posY = "random(0.5, 2)";
            posZ = "random(-1, 1)";
            velX = "0";
            velY = "-0.5";
            velZ = "0";
            size = "0.4 * (1 - life)";
            alpha = "1 - life * life";
            colorR = "1.0";
            colorG = "lerp(1.0, 0.2, life)";
            colorB = "lerp(0.0, 0.0, life)";
            count = 80;
            lifetime = 1.5;
            looping = false;
        }
    };

    public static final ParticlePreset RAIN = new ParticlePreset() {
        {
            name = "rain";
            posX = "random(-10, 10)";
            posY = "5";
            posZ = "random(-10, 10)";
            velX = "0.2";
            velY = "-3";
            velZ = "0";
            size = "0.1";
            alpha = "0.6";
            colorR = "0.5";
            colorG = "0.7";
            colorB = "1.0";
            count = 200;
            lifetime = 3.0;
            spawnRate = 50;
        }
    };

    public static final ParticlePreset FIRE = new ParticlePreset() {
        {
            name = "fire";
            posX = "random(-0.3, 0.3)";
            posY = "0";
            posZ = "random(-0.3, 0.3)";
            velX = "random(-0.1, 0.1)";
            velY = "lerp(0.5, 2.0, random())";
            velZ = "random(-0.1, 0.1)";
            size = "lerp(0.5, 0.1, life)";
            alpha = "1 - life";
            colorR = "1.0";
            colorG = "lerp(0.8, 0.0, life)";
            colorB = "0.0";
            count = 50;
            lifetime = 1.0;
            spawnRate = 30;
        }
    };

    public static final ParticlePreset PULSE = new ParticlePreset() {
        {
            name = "pulse";
            posX = "cos(i / count * TAU) * (life * 5)";
            posY = "0";
            posZ = "sin(i / count * TAU) * (life * 5)";
            size = "0.3 * (1 - life)";
            alpha = "1 - life";
            colorR = "0.0";
            colorG = "1.0";
            colorB = "0.5";
            count = 40;
            lifetime = 1.5;
        }
    };

    public static final ParticlePreset GALAXY = new ParticlePreset() {
        {
            name = "galaxy";
            posX = "cos(t * 0.5 + i * 0.3) * (1 + i / count * 5)";
            posY = "sin(i * 1.7) * 0.3";
            posZ = "sin(t * 0.5 + i * 0.3) * (1 + i / count * 5)";
            size = "0.15";
            alpha = "0.8";
            colorR = "lerp(0.8, 0.3, i / count)";
            colorG = "lerp(0.5, 0.2, i / count)";
            colorB = "1.0";
            count = 150;
            lifetime = 5.0;
        }
    };

    public static final Map<String, ParticlePreset> BUILT_IN = new LinkedHashMap<>() {
        {
            put("vortex", VORTEX);
            put("firework", FIREWORK);
            put("rain", RAIN);
            put("fire", FIRE);
            put("pulse", PULSE);
            put("galaxy", GALAXY);
        }
    };

    public ParticleBuilder toBuilder() {
        ParticleBuilder b = ParticleBuilder.create(posX, posY, posZ);
        b.velocity(velX, velY, velZ);
        b.size(size);
        b.alpha(alpha);
        b.color(colorR, colorG, colorB);
        b.count(count);
        b.lifetime(lifetime);
        b.rate(spawnRate);
        b.loop(looping);
        b.withShape(resolveShape());
        return b;
    }

    public static ParticlePreset capture(String name,
            String posX, String posY, String posZ,
            String velX, String velY, String velZ,
            String size, String alpha,
            String colorR, String colorG, String colorB,
            int count, double lifetime) {
        ParticlePreset p = new ParticlePreset();
        p.name = name;
        p.posX = posX;
        p.posY = posY;
        p.posZ = posZ;
        p.velX = velX;
        p.velY = velY;
        p.velZ = velZ;
        p.size = size;
        p.alpha = alpha;
        p.colorR = colorR;
        p.colorG = colorG;
        p.colorB = colorB;
        p.count = count;
        p.lifetime = lifetime;
        return p;
    }

    private Shapes.SpawnFunction resolveShape() {
        switch (shapeName) {
            case "circle":
                return Shapes.circle(shapeParam1);
            case "disc":
                return Shapes.disc(shapeParam1);
            case "sphere":
                return Shapes.sphere(shapeParam1);
            case "ball":
                return Shapes.ball(shapeParam1);
            case "square":
                return Shapes.square(shapeParam1);
            case "cube":
                return Shapes.cube(shapeParam1);
            case "ring":
                return Shapes.ring(shapeParam1, shapeParam2);
            case "spiral":
                return Shapes.spiral(shapeParam1, shapeParam2);
            case "rect":
                return Shapes.rect(shapeParam1, shapeParam2);
            case "box":
                return Shapes.box(shapeParam1, shapeParam2, shapeParam3);
            default:
                return Shapes.point();
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(ParticlePreset preset, String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(preset));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save particle preset: " + filePath, e);
        }
    }

    public static ParticlePreset load(String filePath) {
        try {
            String json = Files.readString(Paths.get(filePath));
            return GSON.fromJson(json, ParticlePreset.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load particle preset: " + filePath, e);
        }
    }

    public static ParticlePreset fromJson(String json) {
        return GSON.fromJson(json, ParticlePreset.class);
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}

