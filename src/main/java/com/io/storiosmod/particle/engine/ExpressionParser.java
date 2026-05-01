package com.io.storiosmod.particle.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ExpressionParser {

    private static final Map<String, Function<Expression[], Expression>> FUNCTIONS = new HashMap<>();

    static {
        reg1("sin", Math::sin);
        reg1("cos", Math::cos);
        reg1("tan", Math::tan);
        reg1("asin", Math::asin);
        reg1("acos", Math::acos);
        reg1("atan", Math::atan);
        reg1("abs", Math::abs);
        reg1("sqrt", Math::sqrt);
        reg1("exp", Math::exp);
        reg1("log", Math::log);
        reg1("log2", v -> Math.log(v) / Math.log(2));
        reg1("floor", Math::floor);
        reg1("ceil", Math::ceil);
        reg1("round", v -> (double) Math.round(v));
        reg1("sign", Math::signum);
        reg1("fract", v -> v - Math.floor(v));
        reg1("degrees", Math::toDegrees);
        reg1("radians", Math::toRadians);

        reg2("pow", Math::pow);
        reg2("min", Math::min);
        reg2("max", Math::max);
        reg2("atan2", Math::atan2);
        reg2("mod", (a, b) -> a - b * Math.floor(a / b));
        reg2("step", (edge, x) -> x < edge ? 0.0 : 1.0);

        FUNCTIONS.put("clamp", args -> {
            if (args.length != 3)
                throw new RuntimeException("clamp(value, min, max) needs 3 args");
            return ctx -> Math.max(args[1].eval(ctx), Math.min(args[2].eval(ctx), args[0].eval(ctx)));
        });
        FUNCTIONS.put("lerp", args -> {
            if (args.length != 3)
                throw new RuntimeException("lerp(a, b, t) needs 3 args");
            return ctx -> {
                double a = args[0].eval(ctx);
                double b = args[1].eval(ctx);
                double tt = args[2].eval(ctx);
                return a + (b - a) * tt;
            };
        });
        FUNCTIONS.put("smoothstep", args -> {
            if (args.length != 3)
                throw new RuntimeException("smoothstep(edge0, edge1, x) needs 3 args");
            return ctx -> {
                double edge0 = args[0].eval(ctx);
                double edge1 = args[1].eval(ctx);
                double x = args[2].eval(ctx);
                double tt = Math.max(0, Math.min(1, (x - edge0) / (edge1 - edge0)));
                return tt * tt * (3 - 2 * tt);
            };
        });
        FUNCTIONS.put("map", args -> {
            if (args.length != 5)
                throw new RuntimeException("map(value, inMin, inMax, outMin, outMax) needs 5 args");
            return ctx -> {
                double value = args[0].eval(ctx);
                double inMin = args[1].eval(ctx);
                double inMax = args[2].eval(ctx);
                double outMin = args[3].eval(ctx);
                double outMax = args[4].eval(ctx);
                return outMin + (outMax - outMin) * ((value - inMin) / (inMax - inMin));
            };
        });

        FUNCTIONS.put("random", args -> {
            if (args.length == 0)
                return ctx -> ThreadLocalRandom.current().nextDouble();
            if (args.length == 1)
                return ctx -> ThreadLocalRandom.current().nextDouble() * args[0].eval(ctx);
            if (args.length == 2)
                return ctx -> {
                    double lo = args[0].eval(ctx);
                    double hi = args[1].eval(ctx);
                    return lo + ThreadLocalRandom.current().nextDouble() * (hi - lo);
                };
            throw new RuntimeException("random() takes 0-2 args");
        });

        FUNCTIONS.put("noise", args -> {
            if (args.length < 1 || args.length > 2)
                throw new RuntimeException("noise(x) or noise(x, y)");
            if (args.length == 1) {
                return ctx -> {
                    double x = args[0].eval(ctx);
                    return noise1D(x);
                };
            }
            return ctx -> {
                double x = args[0].eval(ctx);
                double y = args[1].eval(ctx);
                return noise2D(x, y);
            };
        });
    }

    private static void reg1(String name, Function<Double, Double> fn) {
        FUNCTIONS.put(name, args -> {
            if (args.length != 1)
                throw new RuntimeException(name + "() needs 1 arg");
            return ctx -> fn.apply(args[0].eval(ctx));
        });
    }

    private static void reg2(String name, BiFunction<Double, Double, Double> fn) {
        FUNCTIONS.put(name, args -> {
            if (args.length != 2)
                throw new RuntimeException(name + "() needs 2 args");
            return ctx -> fn.apply(args[0].eval(ctx), args[1].eval(ctx));
        });
    }

    private static double noise1D(double x) {
        int ix = (int) Math.floor(x);
        double fx = x - ix;
        fx = fx * fx * (3.0 - 2.0 * fx);
        return lerp(hash1(ix), hash1(ix + 1), fx) * 2.0 - 1.0;
    }

    private static double noise2D(double x, double y) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double fx = x - ix;
        double fy = y - iy;
        fx = fx * fx * (3.0 - 2.0 * fx);
        fy = fy * fy * (3.0 - 2.0 * fy);
        double a = lerp(hash2(ix, iy), hash2(ix + 1, iy), fx);
        double b = lerp(hash2(ix, iy + 1), hash2(ix + 1, iy + 1), fx);
        return lerp(a, b, fy) * 2.0 - 1.0;
    }

    private static double hash1(int n) {
        n = (n << 13) ^ n;
        return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / (double) 0x7fffffff;
    }

    private static double hash2(int x, int y) {
        return hash1(x + y * 57);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static Expression parse(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return Expression.constant(0);
        }
        Parser p = new Parser(formula.trim());
        Expression expr = p.parseTernary();
        if (p.pos < p.input.length()) {
            throw new RuntimeException(
                    "Unexpected char at pos " + p.pos + ": '" + p.input.charAt(p.pos) + "' in: " + formula);
        }
        return expr;
    }

    private static class Parser {
        final String input;
        int pos = 0;

        Parser(String input) {
            this.input = input;
        }

        void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos)))
                pos++;
        }

        char peek() {
            skipWhitespace();
            return pos < input.length() ? input.charAt(pos) : '\0';
        }

        boolean match(char c) {
            if (peek() == c) {
                pos++;
                return true;
            }
            return false;
        }

        boolean match(String s) {
            skipWhitespace();
            if (input.startsWith(s, pos)) {
                pos += s.length();
                return true;
            }
            return false;
        }

        Expression parseTernary() {
            Expression cond = parseComparison();
            skipWhitespace();
            if (match('?')) {
                Expression trueExpr = parseTernary();
                if (!match(':'))
                    throw new RuntimeException("Expected ':' in ternary at pos " + pos);
                Expression falseExpr = parseTernary();
                return ctx -> cond.eval(ctx) != 0 ? trueExpr.eval(ctx) : falseExpr.eval(ctx);
            }
            return cond;
        }

        Expression parseComparison() {
            Expression left = parseAddSub();
            while (true) {
                skipWhitespace();
                if (match(">=")) {
                    Expression r = parseAddSub();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) >= r.eval(ctx) ? 1.0 : 0.0;
                } else if (match("<=")) {
                    Expression r = parseAddSub();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) <= r.eval(ctx) ? 1.0 : 0.0;
                } else if (match("==")) {
                    Expression r = parseAddSub();
                    Expression l = left;
                    left = ctx -> Math.abs(l.eval(ctx) - r.eval(ctx)) < 1e-9 ? 1.0 : 0.0;
                } else if (match("!=")) {
                    Expression r = parseAddSub();
                    Expression l = left;
                    left = ctx -> Math.abs(l.eval(ctx) - r.eval(ctx)) >= 1e-9 ? 1.0 : 0.0;
                } else if (peek() == '>' && !(pos + 1 < input.length() && input.charAt(pos + 1) == '=')) {
                    pos++;
                    Expression r = parseAddSub();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) > r.eval(ctx) ? 1.0 : 0.0;
                } else if (peek() == '<' && !(pos + 1 < input.length() && input.charAt(pos + 1) == '=')) {
                    pos++;
                    Expression r = parseAddSub();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) < r.eval(ctx) ? 1.0 : 0.0;
                } else {
                    break;
                }
            }
            return left;
        }

        Expression parseAddSub() {
            Expression left = parseMulDiv();
            while (true) {
                if (match('+')) {
                    Expression r = parseMulDiv();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) + r.eval(ctx);
                } else if (peek() == '-' && !isUnaryMinus()) {
                    pos++;
                    Expression r = parseMulDiv();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) - r.eval(ctx);
                } else {
                    break;
                }
            }
            return left;
        }

        private boolean isUnaryMinus() {
            return false;
        }

        Expression parseMulDiv() {
            Expression left = parsePower();
            while (true) {
                if (match('*')) {
                    Expression r = parsePower();
                    Expression l = left;
                    left = ctx -> l.eval(ctx) * r.eval(ctx);
                } else if (match('/')) {
                    Expression r = parsePower();
                    Expression l = left;
                    left = ctx -> {
                        double rv = r.eval(ctx);
                        return rv == 0 ? 0 : l.eval(ctx) / rv;
                    };
                } else if (match('%')) {
                    Expression r = parsePower();
                    Expression l = left;
                    left = ctx -> {
                        double rv = r.eval(ctx);
                        return rv == 0 ? 0 : l.eval(ctx) % rv;
                    };
                } else {
                    break;
                }
            }
            return left;
        }

        Expression parsePower() {
            Expression base = parseUnary();
            if (match('^')) {
                Expression exp = parseUnary();
                return ctx -> Math.pow(base.eval(ctx), exp.eval(ctx));
            }
            return base;
        }

        Expression parseUnary() {
            if (match('-')) {
                Expression inner = parseUnary();
                return ctx -> -inner.eval(ctx);
            }
            if (match('+')) {
                return parseUnary();
            }
            return parseAtom();
        }

        Expression parseAtom() {
            skipWhitespace();

            if (match('(')) {
                Expression inner = parseTernary();
                if (!match(')'))
                    throw new RuntimeException("Missing ')' at pos " + pos);
                return inner;
            }

            if (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                return parseNumber();
            }

            if (pos < input.length() && (Character.isLetter(input.charAt(pos)) || input.charAt(pos) == '_')) {
                return parseIdentifier();
            }

            throw new RuntimeException("Unexpected char at pos " + pos + ": '" +
                    (pos < input.length() ? input.charAt(pos) : "EOF") + "' in: " + input);
        }

        Expression parseNumber() {
            int start = pos;
            while (pos < input.length() && (Character.isDigit(input.charAt(pos)) || input.charAt(pos) == '.')) {
                pos++;
            }
            if (pos < input.length() && (input.charAt(pos) == 'e' || input.charAt(pos) == 'E')) {
                pos++;
                if (pos < input.length() && (input.charAt(pos) == '+' || input.charAt(pos) == '-'))
                    pos++;
                while (pos < input.length() && Character.isDigit(input.charAt(pos)))
                    pos++;
            }
            double val = Double.parseDouble(input.substring(start, pos));
            return Expression.constant(val);
        }

        Expression parseIdentifier() {
            int start = pos;
            while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
                pos++;
            }
            String name = input.substring(start, pos);
            skipWhitespace();

            if (peek() == '(') {
                pos++;
                java.util.List<Expression> argsList = new java.util.ArrayList<>();
                if (peek() != ')') {
                    argsList.add(parseTernary());
                    while (match(',')) {
                        argsList.add(parseTernary());
                    }
                }
                if (!match(')'))
                    throw new RuntimeException("Missing ')' after function '" + name + "' at pos " + pos);

                Function<Expression[], Expression> factory = FUNCTIONS.get(name);
                if (factory == null)
                    throw new RuntimeException("Unknown function: " + name);
                return factory.apply(argsList.toArray(new Expression[0]));
            }

            return Expression.variable(name);
        }
    }
}

