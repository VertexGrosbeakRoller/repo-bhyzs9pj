package copperhead.client.implement.features.modules.combat;

import copperhead.client.common.util.math.GCDUtil;
import net.minecraft.client.Minecraft;

public final class FunTimeRotation {
    private static int hitCounter = 0;
    private static long lastHitTime = 0;
    private static boolean wasAttacking = false;

    public static float[] compute(float currentYaw, float currentPitch,
                                  float targetYaw, float targetPitch,
                                  boolean canAttack, long nowMs) {

        float deltaYaw = wrapTo180(targetYaw - currentYaw);
        float deltaPitch = wrapTo180(targetPitch - currentPitch);
        float total = (float) Math.hypot(deltaYaw, deltaPitch);

        if (total < 0.001f) {
            return new float[]{currentYaw, currentPitch};
        }

        // cap 130° по прямой
        float maxStepYaw = (Math.abs(deltaYaw) / total) * 130f;
        float maxStepPitch = (Math.abs(deltaPitch) / total) * 130f;

        float stepYaw = clamp(deltaYaw, -maxStepYaw, maxStepYaw);
        float stepPitch = clamp(deltaPitch, -maxStepPitch, maxStepPitch);

        float nextYaw = currentYaw + stepYaw;
        float nextPitch = currentPitch + stepPitch;

        // Детект реального удара (Rising Edge)
        boolean isNewHit = canAttack && !wasAttacking;
        if (isNewHit) {
            hitCounter++;
            lastHitTime = nowMs;
        }
        wasAttacking = canAttack;

        if (canAttack) {
            // attack: сглаживание 0.85
            nextYaw = lerp(0.85f, currentYaw, nextYaw);
            nextPitch = lerp(0.85f, currentPitch, nextPitch);

            // Флик вниз каждый 86-й хит в окне 250 мс
            if (isNewHit && hitCounter % 86 == 0 && (nowMs - lastHitTime) < 250) {
                nextPitch = -90f;
            }

        } else {
            // idle shake
            long sinceLastHit = nowMs - lastHitTime;

            if (sinceLastHit >= 535) {
                float shakeYaw = (18f + (float) Math.random() * 10f) * (float) Math.sin(nowMs / 60.0);
                float shakePitch = (6f + (float) Math.random() * 10f) * (float) Math.cos(nowMs / 60.0);

                nextYaw = clamp(currentYaw + shakeYaw, currentYaw - 45f, currentYaw + 45f);
                nextPitch = clamp(currentPitch + shakePitch, currentPitch - 45f, currentPitch + 45f);
            } else {
                nextYaw = currentYaw;
                nextPitch = currentPitch;
            }
        }

        // Глобальный кламп pitch
        nextPitch = clamp(nextPitch, -89f, 90f);

        // GCD Snap
        float sens = (float) Minecraft.getInstance().gameSettings.mouseSensitivity;
        nextYaw = GCDUtil.gcdSnap(nextYaw, sens);
        nextPitch = GCDUtil.gcdSnap(nextPitch, sens);

        return new float[]{nextYaw, nextPitch};
    }

    private static float wrapTo180(float v) {
        v %= 360;
        if (v >= 180) v -= 360;
        if (v < -180) v += 360;
        return v;
    }

    private static float clamp(float v, float min, float max) {
        return Math.min(max, Math.max(min, v));
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
}
