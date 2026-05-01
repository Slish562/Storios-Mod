package com.io.storiosmod.particle.engine;

@FunctionalInterface
public interface Expression {
    double eval(ExprContext ctx);

    static Expression constant(double value) {
        return ctx -> value;
    }

    static Expression variable(String name) {
        return ctx -> ctx.get(name);
    }

    static Expression parse(String formula) {
        return ExpressionParser.parse(formula);
    }
}
