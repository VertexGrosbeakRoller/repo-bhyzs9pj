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

/**
 * Snap — очень лёгкая тряска головы + наведение на игрока при ударе.
 * Головотряска еле заметна, чтобы выглядеть максимально легитно.
 */
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

        // Light head shake (very subtle)
        if (ThreadLocalRandom.current().nextFloat() < shakeFrequency.get()) {
            float shakeYaw = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 2.0f * shakeStrength.get();
            float shakePitch = (ThreadLocalRandom.current().nextFloat() - 0.5f) * 2.0f * shakeStrength.get() * 0.5f;

            float sens = (float) mc.gameSettings.mouseSensitivity;
            mc.player.rotationYaw += GCDUtil.gcdSnap(shakeYaw, sens);
            mc.player.rotationPitch += GCDUtil.gcdSnap(shakePitch, sens);
            mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90, 90);
        }

        // Snap to target on hit
        if (snapTarget != null && snapTicks > 0) {
            snapTicks--;
            float targetYaw = getYawToEntity(snapTarget);
            float targetPitch = getPitchToEntity(snapTarget);

            float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.rotationYaw);
            float pitchDiff = targetPitch - mc.player.rotationPitch;

            float speed = snapSpeed.get();
            mc.player.rotationYaw += yawDiff * speed;
            mc.player.rotationPitch += pitchDiff * speed;
            mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90, 90);

            float sens = (float) mc.gameSettings.mouseSensitivity;
            mc.player.rotationYaw = GCDUtil.gcdSnap(mc.player.rotationYaw, sens);
            mc.player.rotationPitch = GCDUtil.gcdSnap(mc.player.rotationPitch, sens);

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
        if (target instanceof LivingEntity && mc.player.getDistance(target) <= snapDistance.get()) {
            snapTarget = (LivingEntity) target;
            snapTicks = SNAP_DURATION;
        }
    }

    private float getYawToEntity(Entity entity) {
        double dx = entity.getPosX() - mc.player.getPosX();
        double dz = entity.getPosZ() - mc.player.getPosZ();
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
    }

    private float getPitchToEntity(Entity entity) {
        double dx = entity.getPosX() - mc.player.getPosX();
        double dy = (entity.getPosY() + entity.getEyeHeight()) - (mc.player.getPosY() + mc.player.getEyeHeight());
        double dz = entity.getPosZ() - mc.player.getPosZ();
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
