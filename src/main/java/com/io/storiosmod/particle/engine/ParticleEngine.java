package com.io.storiosmod.particle.engine;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleEngine {

    private static ParticleEngine INSTANCE;

    private final Map<String, ParticleEmitter> emitters = new ConcurrentHashMap<>();
    private int nextId = 0;

    public static ParticleEngine getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ParticleEngine();
        return INSTANCE;
    }

    public String add(ParticleEmitter emitter) {
        String id = "pe_" + (nextId++);
        emitters.put(id, emitter);
        return id;
    }

    public String add(String id, ParticleEmitter emitter) {
        emitters.put(id, emitter);
        return id;
    }

    public void remove(String id) {
        emitters.remove(id);
    }

    public ParticleEmitter get(String id) {
        return emitters.get(id);
    }

    public Collection<ParticleEmitter> all() {
        return emitters.values();
    }

    public void tick(double dt) {
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, ParticleEmitter> entry : emitters.entrySet()) {
            ParticleEmitter em = entry.getValue();
            em.update(dt);
            if (!em.isActive() && em.getParticles().isEmpty()) {
                toRemove.add(entry.getKey());
            }
        }
        toRemove.forEach(emitters::remove);
    }

    public void tick() {
        tick(1.0 / 20.0);
    }

    public void clear() {
        emitters.clear();
    }

    public int count() {
        return emitters.size();
    }

    public int totalParticles() {
        return emitters.values().stream().mapToInt(e -> e.getParticles().size()).sum();
    }
}

