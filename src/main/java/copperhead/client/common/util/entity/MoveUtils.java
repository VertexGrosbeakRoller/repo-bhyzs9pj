package copperhead.client.common.util.entity;

import net.minecraft.client.Minecraft;

public final class MoveUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    private MoveUtils() {}

    public static boolean isMoving() {
        if (mc.player == null) return false;
        return mc.player.input.forwardImpulse != 0 || mc.player.input.leftImpulse != 0;
    }

    public static double getSpeed() {
        if (mc.player == null) return 0;
        double dx = mc.player.getDeltaMovement().x;
        double dz = mc.player.getDeltaMovement().z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
