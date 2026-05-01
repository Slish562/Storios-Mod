package com.io.storiosmod.particle.engine;

public class Shapes {

    @FunctionalInterface
    public interface SpawnFunction {
        void apply(Particle p, int index, int total, double time);
    }

    public static SpawnFunction point() {
        return (p, i, total, t) -> {
            p.x = 0;
            p.y = 0;
            p.z = 0;
        };
    }

    public static SpawnFunction circle(double radius) {
        return (p, i, total, t) -> {
            double angle = (total > 0) ? ((double) i / total) * Math.PI * 2 : 0;
            p.x = Math.cos(angle) * radius;
            p.y = Math.sin(angle) * radius;
            p.z = 0;
        };
    }

    public static SpawnFunction disc(double radius) {
        return (p, i, total, t) -> {
            double angle = Math.random() * Math.PI * 2;
            double r = radius * Math.sqrt(Math.random());
            p.x = Math.cos(angle) * r;
            p.y = Math.sin(angle) * r;
            p.z = 0;
        };
    }

    public static SpawnFunction sphere(double radius) {
        return (p, i, total, t) -> {
            double theta = Math.acos(2 * Math.random() - 1);
            double phi = Math.random() * Math.PI * 2;
            p.x = Math.sin(theta) * Math.cos(phi) * radius;
            p.y = Math.sin(theta) * Math.sin(phi) * radius;
            p.z = Math.cos(theta) * radius;
        };
    }

    public static SpawnFunction ball(double radius) {
        return (p, i, total, t) -> {
            double theta = Math.acos(2 * Math.random() - 1);
            double phi = Math.random() * Math.PI * 2;
            double r = radius * Math.cbrt(Math.random());
            p.x = Math.sin(theta) * Math.cos(phi) * r;
            p.y = Math.sin(theta) * Math.sin(phi) * r;
            p.z = Math.cos(theta) * r;
        };
    }

    public static SpawnFunction square(double size) {
        return rect(size, size);
    }

    public static SpawnFunction rect(double w, double h) {
        return (p, i, total, t) -> {
            double halfW = w / 2, halfH = h / 2;
            double perimeter = 2 * (w + h);
            double pos = ((double) i / Math.max(total, 1)) * perimeter;
            if (pos < w) {
                p.x = -halfW + pos;
                p.y = -halfH;
            } else if (pos < w + h) {
                p.x = halfW;
                p.y = -halfH + (pos - w);
            } else if (pos < 2 * w + h) {
                p.x = halfW - (pos - w - h);
                p.y = halfH;
            } else {
                p.x = -halfW;
                p.y = halfH - (pos - 2 * w - h);
            }
            p.z = 0;
        };
    }

    public static SpawnFunction cube(double size) {
        return box(size, size, size);
    }

    public static SpawnFunction box(double w, double h, double d) {
        return (p, i, total, t) -> {
            double halfW = w / 2, halfH = h / 2, halfD = d / 2;
            int face = (int) (Math.random() * 6);
            double u = Math.random() * 2 - 1;
            double v = Math.random() * 2 - 1;
            switch (face) {
                case 0:
                    p.x = halfW;
                    p.y = u * halfH;
                    p.z = v * halfD;
                    break;
                case 1:
                    p.x = -halfW;
                    p.y = u * halfH;
                    p.z = v * halfD;
                    break;
                case 2:
                    p.x = u * halfW;
                    p.y = halfH;
                    p.z = v * halfD;
                    break;
                case 3:
                    p.x = u * halfW;
                    p.y = -halfH;
                    p.z = v * halfD;
                    break;
                case 4:
                    p.x = u * halfW;
                    p.y = v * halfH;
                    p.z = halfD;
                    break;
                case 5:
                    p.x = u * halfW;
                    p.y = v * halfH;
                    p.z = -halfD;
                    break;
            }
        };
    }

    public static SpawnFunction line(double x1, double y1, double z1,
            double x2, double y2, double z2) {
        return (p, i, total, t) -> {
            double f = total > 1 ? (double) i / (total - 1) : 0.5;
            p.x = x1 + (x2 - x1) * f;
            p.y = y1 + (y2 - y1) * f;
            p.z = z1 + (z2 - z1) * f;
        };
    }

    public static SpawnFunction ring(double radius, double thickness) {
        return (p, i, total, t) -> {
            double angle = (total > 0) ? ((double) i / total) * Math.PI * 2 : 0;
            double r = radius + (Math.random() - 0.5) * thickness;
            p.x = Math.cos(angle) * r;
            p.y = Math.sin(angle) * r;
            p.z = 0;
        };
    }

    public static SpawnFunction spiral(double radius, double turns) {
        return (p, i, total, t) -> {
            double progress = total > 0 ? (double) i / total : 0;
            double angle = progress * Math.PI * 2 * turns;
            double r = radius * progress;
            p.x = Math.cos(angle) * r;
            p.y = Math.sin(angle) * r;
            p.z = 0;
        };
    }

    public static SpawnFunction grid(int cols, int rows, double spacing) {
        return (p, i, total, t) -> {
            int col = i % cols;
            int row = (i / cols) % rows;
            p.x = (col - (cols - 1) * 0.5) * spacing;
            p.y = (row - (rows - 1) * 0.5) * spacing;
            p.z = 0;
        };
    }
}
