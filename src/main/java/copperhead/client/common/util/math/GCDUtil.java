package copperhead.client.common.util.math;

public final class GCDUtil {
    private GCDUtil() {}

    public static float gcdSnap(float value, float sensitivity) {
        float f = (float) (sensitivity * 0.6F + 0.2F);
        float gcd = f * f * f * 1.2F;
        return (float) (Math.round(value / gcd) * gcd);
    }
}
