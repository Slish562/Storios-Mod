package com.io.storiosmod.cutscene;

import net.minecraft.world.phys.Vec3;

public enum InterpolationType {
    LINEAR {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            return p1.lerp(p2, t);
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return lerpAngle(a1, a2, t);
        }
    },
    CATMULL_ROM {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            double x = catmull(p0.x, p1.x, p2.x, p3.x, t);
            double y = catmull(p0.y, p1.y, p2.y, p3.y, t);
            double z = catmull(p0.z, p1.z, p2.z, p3.z, t);
            return new Vec3(x, y, z);
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return (float) catmull(a0, a1, a2, a3, t);
        }
    },
    EASE_IN_OUT {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            return p1.lerp(p2, smoothstep(t));
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return lerpAngle(a1, a2, smoothstep(t));
        }
    },
    EASE_IN {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            return p1.lerp(p2, t * t * t);
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return lerpAngle(a1, a2, t * t * t);
        }
    },
    EASE_OUT {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            float inv = 1 - t;
            return p1.lerp(p2, 1 - inv * inv * inv);
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            float inv = 1 - t;
            return lerpAngle(a1, a2, 1 - inv * inv * inv);
        }
    },
    SMOOTH {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            return p1.lerp(p2, smootherstep(t));
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return lerpAngle(a1, a2, smootherstep(t));
        }
    },
    BEZIER {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            double x = bezier(p0.x, p1.x, p2.x, p3.x, t);
            double y = bezier(p0.y, p1.y, p2.y, p3.y, t);
            double z = bezier(p0.z, p1.z, p2.z, p3.z, t);
            return new Vec3(x, y, z);
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return (float) bezier(a0, a1, a2, a3, t);
        }
    },
    HOLD {
        @Override
        public Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
            return p1;
        }

        @Override
        public float interpolateAngle(float a0, float a1, float a2, float a3, float t) {
            return a1;
        }
    };

    public abstract Vec3 interpolate(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t);

    public abstract float interpolateAngle(float a0, float a1, float a2, float a3, float t);

    protected static float lerpAngle(float a, float b, float t) {
        float diff = ((b - a) % 360 + 540) % 360 - 180;
        return a + diff * t;
    }

    private static float smoothstep(float t) {
        return t * t * (3 - 2 * t);
    }

    private static float smootherstep(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double catmull(double p0, double p1, double p2, double p3, float t) {
        double t2 = t * t;
        double t3 = t2 * t;
        return 0.5 * ((2 * p1) +
                (-p0 + p2) * t +
                (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 +
                (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
    }

    private static double bezier(double p0, double p1, double p2, double p3, float t) {
        double inv = 1 - t;
        double inv2 = inv * inv;
        double inv3 = inv2 * inv;
        double t2 = (double) t * t;
        double t3 = t2 * t;
        return inv3 * p0 + 3 * inv2 * t * p1 + 3 * inv * t2 * p2 + t3 * p3;
    }
}
