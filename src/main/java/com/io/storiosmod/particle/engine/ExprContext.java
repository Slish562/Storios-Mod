package com.io.storiosmod.particle.engine;

import java.util.HashMap;
import java.util.Map;

public class ExprContext {
    private final Map<String, Double> variables = new HashMap<>();

    public ExprContext() {
        variables.put("PI", Math.PI);
        variables.put("TAU", Math.PI * 2.0);
        variables.put("E", Math.E);
        variables.put("t", 0.0);
        variables.put("dt", 0.0);
        variables.put("i", 0.0);
        variables.put("count", 0.0);
        variables.put("life", 0.0);
        variables.put("age", 0.0);
        variables.put("x", 0.0);
        variables.put("y", 0.0);
        variables.put("z", 0.0);
        variables.put("vx", 0.0);
        variables.put("vy", 0.0);
        variables.put("vz", 0.0);
        variables.put("size", 1.0);
        variables.put("r", 1.0);
        variables.put("g", 1.0);
        variables.put("b", 1.0);
        variables.put("a", 1.0);
    }

    public ExprContext set(String name, double value) {
        variables.put(name, value);
        return this;
    }

    public double get(String name) {
        Double v = variables.get(name);
        return v != null ? v : 0.0;
    }

    public boolean has(String name) {
        return variables.containsKey(name);
    }

    public ExprContext copy() {
        ExprContext ctx = new ExprContext();
        ctx.variables.putAll(this.variables);
        return ctx;
    }
}
