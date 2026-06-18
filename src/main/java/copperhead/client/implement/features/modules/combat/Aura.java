package copperhead.client.implement.features.modules.combat;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.implement.events.EventUpdate;
import copperhead.client.managers.FriendManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Aura extends Module {

    private final SliderSetting range = new SliderSetting("Дистанция", 3.6f, 2.0f, 6.0f, 0.1f);
    private final ModeSetting rotationMode = new ModeSetting("Ротация", "FunTime", "FunTime", "Snap", "None");
    private final BooleanSetting onlyPlayers = new BooleanSetting("Только игроки", true);
    private final BooleanSetting ignoreTeam = new BooleanSetting("Игнор. тиму", true);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Игнор. друзей", true);
    private final BooleanSetting autoBlock = new BooleanSetting("Авто блок", false);

    private LivingEntity target;
    private float lastYaw, lastPitch;

    public Aura() {
        super("Aura", "Aura", ModuleCategory.COMBAT);
        addSettings(range, rotationMode, onlyPlayers, ignoreTeam, ignoreFriends, autoBlock);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.level == null) return;

        target = findTarget();
        if (target == null) return;

        if (rotationMode.is("FunTime")) {
            float targetYaw = getYawToEntity(target);
            float targetPitch = getPitchToEntity(target);
            boolean canAttack = mc.player.getAttackStrengthScale(0) >= 1.0f;

            float[] result = FunTimeRotation.compute(
                    lastYaw != 0 ? lastYaw : mc.player.yRot,
                    lastPitch != 0 ? lastPitch : mc.player.xRot,
                    targetYaw, targetPitch,
                    canAttack, System.currentTimeMillis()
            );

            mc.player.yRot = result[0];
            mc.player.xRot = result[1];
            lastYaw = result[0];
            lastPitch = result[1];
        } else if (rotationMode.is("Snap")) {
            mc.player.yRot = getYawToEntity(target);
            mc.player.xRot = getPitchToEntity(target);
        }

        if (mc.player.getAttackStrengthScale(0) >= 1.0f) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(Hand.MAIN_HAND);
        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> entities = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .filter(e -> e != mc.player)
                .filter(e -> e.isAlive())
                .filter(e -> mc.player.distanceTo(e) <= range.get())
                .filter(e -> !onlyPlayers.get() || e instanceof PlayerEntity)
                .filter(e -> !(e instanceof PlayerEntity &&
                        ignoreFriends.get() &&
                        FriendManager.isFriend(e.getName().getString())))
                .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                .collect(Collectors.toList());

        return entities.isEmpty() ? null : entities.get(0);
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

    public LivingEntity getTarget() { return target; }

    @Override
    public boolean onEnable() {
        target = null;
        lastYaw = 0;
        lastPitch = 0;
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        target = null;
        super.onDisable();
    }
}
