package copperhead.client.implement.features.modules.combat;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.math.GCDUtil;
import copperhead.client.implement.events.EventDamage;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.ThreadLocalRandom;

public class Snap extends Module {

    private final SliderSetting shakeStrength = new SliderSetting("Сила тряски", 0.3f, 0.05f, 2.0f, 0.05f);
    private final SliderSetting snapSpeed = new SliderSetting("Скорость наведения", 0.7f, 0.1f, 1.0f, 0.05f);
    private final SliderSetting snapDistance = new SliderSetting("Дистанция наведения", 4.0f, 2.0f, 6.0f, 0.1f);
    private final SliderSetting shakeFrequency = new SliderSetting("Частота тряски", 0.1f, 0.01f, 0.5f, 0.01f);

    private LivingEntity snapTarget = null;
    private int snapTicks = 0;
    private static final int SNAP_DURATION = 8;

    public Snap() {
        super("Snap", "Snap", ModuleCategory.COMBAT);
        addSettings(shakeStrength, snapSpeed, snapDistance, shakeFrequency);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        if (ThreadLocalRandom.current().nextFloat() < shakeFrequency.get()) {
            float shakeYaw = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 2.0f * shakeStrength.get();
            float shakePitch = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 2.0f * shakeStrength.get() * 0.5f;

            float sens = (float)(double) mc.options.sensitivity;
            mc.player.yRot += GCDUtil.gcdSnap(shakeYaw, sens);
            mc.player.xRot += GCDUtil.gcdSnap(shakePitch, sens);
            mc.player.xRot = MathHelper.clamp(mc.player.xRot, -90, 90);
        }

        if (snapTarget != null && snapTicks > 0) {
            snapTicks--;
            float targetYaw = getYawToEntity(snapTarget);
            float targetPitch = getPitchToEntity(snapTarget);

            float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.yRot);
            float pitchDiff = targetPitch - mc.player.xRot;

            float speed = snapSpeed.get();
            mc.player.yRot += yawDiff * speed;
            mc.player.xRot += pitchDiff * speed;
            mc.player.xRot = MathHelper.clamp(mc.player.xRot, -90, 90);

            float sens = (float)(double) mc.options.sensitivity;
            mc.player.yRot = GCDUtil.gcdSnap(mc.player.yRot, sens);
            mc.player.xRot = GCDUtil.gcdSnap(mc.player.xRot, sens);

            if (snapTicks <= 0) {
                snapTarget = null;
            }
        }
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        if (event.getAttacker() != mc.player) return;

        Entity target = event.getTarget();
        if (target instanceof LivingEntity && mc.player.distanceTo(target) <= snapDistance.get()) {
            snapTarget = (LivingEntity) target;
            snapTicks = SNAP_DURATION;
        }
    }

    private float getYawToEntity(Entity entity) {
        double dx = entity.getX() - mc.player.getX();
        double dz = entity.getZ() - mc.player.getZ();
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    private float getPitchToEntity(Entity entity) {
        double dx = entity.getX() - mc.player.getX();
        double dy = (entity.getY() + entity.getEyeHeight()) - (mc.player.getY() + mc.player.getEyeHeight());
        double dz = entity.getZ() - mc.player.getZ();
        double dist = MathHelper.sqrt(dx * dx + dz * dz);
        return (float) (-Math.toDegrees(Math.atan2(dy, dist)));
    }

    @Override
    public boolean onEnable() {
        snapTarget = null;
        snapTicks = 0;
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        snapTarget = null;
        snapTicks = 0;
        super.onDisable();
    }
}
