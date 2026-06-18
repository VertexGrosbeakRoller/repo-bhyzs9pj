package copperhead.client.common.util.math;

import net.minecraft.entity.Entity;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtils {
    private MathUtils() {}

    public static float random(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    public static float random1(float min, float max) {
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    public static double interpolate(double current, double old, float partialTicks) {
        return old + (current - old) * partialTicks;
    }

    public static double getBps(Entity entity, int ticks) {
        double dx = entity.getX() - entity.xo;
        double dz = entity.getZ() - entity.zo;
        return Math.sqrt(dx * dx + dz * dz) * 20.0 / ticks;
    }

    public static float clamp(float value, float min, float max) {
        return Math.min(max, Math.max(min, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    public static float wrapTo180(float v) {
        v %= 360;
        if (v >= 180) v -= 360;
        if (v < -180) v += 360;
        return v;
    }
}
