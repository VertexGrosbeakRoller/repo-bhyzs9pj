package copperhead.client.common.util.entity;

import net.minecraft.client.Minecraft;

public final class MoveUtils {
    private static final Minecraft mc = Minecraft.getInstance();

    private MoveUtils() {}

    public static boolean isMoving() {
        if (mc.player == null) return false;
        return mc.player.movementInput.moveForward != 0 || mc.player.movementInput.moveStrafe != 0;
    }

    public static double getSpeed() {
        if (mc.player == null) return 0;
        double dx = mc.player.getMotion().x;
        double dz = mc.player.getMotion().z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
