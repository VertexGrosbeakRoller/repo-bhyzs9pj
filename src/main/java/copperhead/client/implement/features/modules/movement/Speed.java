package copperhead.client.implement.features.modules.movement;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.entity.DamageUtil;
import copperhead.client.common.util.entity.MoveUtils;
import copperhead.client.implement.events.EventDamage;
import copperhead.client.implement.events.EventPacket;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class Speed extends Module {
    private final ModeSetting modeSetting = new ModeSetting("Режим", "Strafe",
            "Strafe", "StrafeStrict", "Motion", "Matrix", "GrimCollision", "MetaHvH", "HolyWorld");

    private final BooleanSetting speedInWater = new BooleanSetting("Скорость в воде", false).setVisible(this::isStrafe);
    private final BooleanSetting useTimer = new BooleanSetting("Таймер", false).setVisible(this::isStrafe);
    private final BooleanSetting timerBypass = new BooleanSetting("Обход таймера", true).setVisible(() -> isStrafe() && useTimer.get());
    private final SliderSetting bypassThreshold = new SliderSetting("Порог обхода", 25f, 15f, 30f, 1f).setVisible(() -> isStrafe() && useTimer.get() && timerBypass.get());
    private final SliderSetting timerMultiplier = new SliderSetting("Множитель таймера", 1.08f, 1.0f, 1.2f, 0.01f).setVisible(() -> isStrafe() && useTimer.get());

    private final SliderSetting motionSpeed = new SliderSetting("Скорость", 0.15f, 0.1f, 0.5f, 0.01f).setVisible(() -> modeSetting.is("Motion"));
    private final BooleanSetting autoJump = new BooleanSetting("Авто прыжок", true).setVisible(() -> modeSetting.is("Motion"));

    private final ModeSetting metaMode = new ModeSetting("Meta режим", "Default", "Default", "Custom").setVisible(() -> modeSetting.is("MetaHvH"));
    private final SliderSetting metaSpeedSlider = new SliderSetting("Meta скорость", 0.2f, 0.2f, 1.05f, 0.01f).setVisible(() -> modeSetting.is("MetaHvH") && metaMode.is("Custom"));
    private final BooleanSetting metaDamageBoost = new BooleanSetting("Meta буст с дамагом", false).setVisible(() -> modeSetting.is("MetaHvH"));
    private final SliderSetting metaBoostSpeed = new SliderSetting("Meta значение буста", 0.7f, 0.1f, 5.0f, 0.1f).setVisible(() -> modeSetting.is("MetaHvH") && metaDamageBoost.get());
    private final SliderSetting metaBoostDuration = new SliderSetting("Meta длительность буста", 700f, 100f, 2000f, 100f).setVisible(() -> modeSetting.is("MetaHvH") && metaDamageBoost.get());

    private final SliderSetting speedValue = new SliderSetting("Скорость буста", 8.0f, 0.1f, 8.0f, 0.1f).setVisible(() -> modeSetting.is("GrimCollision"));
    private final SliderSetting growValueSlider = new SliderSetting("Радиус", 1.0f, 0.5f, 1.5f, 0.1f).setVisible(() -> modeSetting.is("GrimCollision"));
    private final SliderSetting holySpeedSlider = new SliderSetting("Скорость", 0.35f, 0.3f, 1.0f, 0.05f).setVisible(() -> modeSetting.is("HolyWorld"));
    private final SliderSetting holyRadiusSlider = new SliderSetting("Дистанция", 0.35f, 0.2f, 0.95f, 0.01f).setVisible(() -> modeSetting.is("HolyWorld"));

    private final DamageUtil metaDamageUtil = new DamageUtil();

    private double distance;
    private double strafeSpeed;
    private int stage = 1;
    private int ticks;
    private int holyBoostTicks;
    private float lastForward;

    private static final int HOLY_BURST_ON_TICKS = 5;
    private static final int HOLY_BURST_OFF_TICKS = 20;

    public Speed() {
        super("Speed", "Speed", ModuleCategory.MOVEMENT);
        addSettings(modeSetting, speedInWater, useTimer, timerBypass, bypassThreshold, timerMultiplier,
                motionSpeed, autoJump, metaMode, metaSpeedSlider, metaDamageBoost, metaBoostSpeed, metaBoostDuration,
                speedValue, growValueSlider, holySpeedSlider, holyRadiusSlider);
    }

    @Override
    public boolean onEnable() {
        stage = 1;
        ticks = 0;
        holyBoostTicks = 0;
        distance = 0.0;
        strafeSpeed = 0.0;
        lastForward = 0.0f;
        return super.onEnable();
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        switch (modeSetting.get()) {
            case "Strafe": handleStrafe(false); break;
            case "StrafeStrict": handleStrafe(true); break;
            case "Motion": handleMotion(); break;
            case "Matrix": handleMatrix(); break;
            case "GrimCollision": handleGrimCollision(); break;
            case "MetaHvH": handleMetaHvH(); break;
            case "HolyWorld": handleHolyWorld(); break;
        }
    }

    @EventHandler
    public void onPacket(EventPacket e) {
        if (modeSetting.is("MetaHvH") && metaDamageBoost.get()) {
            metaDamageUtil.onPacketEvent(e);
        }
    }

    @EventHandler
    public void onDamage(EventDamage e) {
        if (modeSetting.is("MetaHvH") && metaDamageBoost.get()) {
            metaDamageUtil.processDamage(e);
        }
    }

    private void handleStrafe(boolean strict) {
        boolean inFluid = mc.player.isInWater() || mc.player.isInLava();
        boolean inCobweb = mc.world.getBlockState(mc.player.getPosition()).getBlock() == Blocks.COBWEB;

        if (mc.player.isSneaking() || mc.player.isElytraFlying() || mc.player.isOnLadder()
                || (inFluid && !speedInWater.get()) || inCobweb
                || mc.player.abilities.isFlying || mc.player.fallDistance >= 5.0f) {
            stage = 1;
            return;
        }

        if (!MoveUtils.isMoving()) { stage = 1; return; }

        double dx = mc.player.getPosX() - mc.player.prevPosX;
        double dz = mc.player.getPosZ() - mc.player.prevPosZ;
        distance = Math.sqrt(dx * dx + dz * dz);

        double base = getPotionSpeed(0.2873);
        float forward = mc.player.movementInput.moveForward;
        strafeSpeed = base * (forward <= 0.0f && lastForward > 0.0f ? 0.66 : 1.0);

        if (stage == 1 && mc.player.isOnGround()) {
            mc.player.setMotion(mc.player.getMotion().x, getPotionJump(0.42), mc.player.getMotion().z);
            strafeSpeed *= 2.149;
            stage = 2;
        } else if (stage == 2) {
            strafeSpeed = distance - (0.66 * (distance - base));
            stage = 3;
        } else {
            if (mc.player.isOnGround()) stage = 1;
            strafeSpeed = distance > 0.0 ? distance - distance / 159.0 : base;
        }

        strafeSpeed = Math.max(strafeSpeed, base);
        double ncpCap = getPotionSpeed(strict || forward < 1.0f ? 0.465 : 0.576);
        double bypassCap = getPotionSpeed(strict || forward < 1.0f ? 0.44 : 0.57);
        strafeSpeed = Math.min(strafeSpeed, ticks > bypassThreshold.get() ? ncpCap : bypassCap);

        if (++ticks > 50) ticks = 0;
        setHorizontalMotion(strafeSpeed);
        lastForward = forward;
    }

    private void handleMotion() {
        if (mc.player.isElytraFlying() || !MoveUtils.isMoving()) return;
        if (mc.player.isOnGround() && autoJump.get()) mc.player.jump();
        setHorizontalMotion(motionSpeed.get());
    }

    private void handleMatrix() {
        if (mc.player.isElytraFlying()) return;
        if (mc.player.isOnGround() && MoveUtils.isMoving()) mc.player.jump();
        Vector3d motion = mc.player.getMotion();
        if (motion.y == -0.4448259643949201) {
            mc.player.setMotion(motion.x * 2.4, motion.y, motion.z * 2.4);
        }
    }

    private void handleGrimCollision() {
        AxisAlignedBB aabb = mc.player.getBoundingBox().grow(growValueSlider.get());
        int armorStands = mc.world.getEntitiesWithinAABB(ArmorStandEntity.class, aabb).size();
        int living = mc.world.getEntitiesWithinAABB(LivingEntity.class, aabb).size();
        boolean canBoost = armorStands > 1 || living > 1;

        if (canBoost && !mc.player.isOnGround()) {
            mc.player.jumpMovementFactor = armorStands > 1
                    ? speedValue.get() / armorStands
                    : speedValue.get() * 0.16f;
        }
    }

    private void handleMetaHvH() {
        if (mc.player.isElytraFlying()) return;

        ItemStack offHandItem = mc.player.getHeldItemOffhand();
        EffectInstance speedEffect = mc.player.getActivePotionEffect(Effects.SPEED);
        EffectInstance slownessEffect = mc.player.getActivePotionEffect(Effects.SLOWNESS);
        String itemName = offHandItem.getDisplayName().getString();
        float speedToApply;

        if (metaMode.is("Default")) {
            if (speedEffect != null) {
                int amp = speedEffect.getAmplifier();
                if (amp == 2) speedToApply = isMetaStrongItem(itemName) ? 0.49665f : 0.41598004f;
                else if (amp == 1) speedToApply = isMetaStrongItem(itemName) ? 0.43f : 0.36f;
                else speedToApply = isMetaStrongItem(itemName) ? 0.2924f : 0.24480002f;
            } else {
                speedToApply = isMetaStrongItem(itemName) ? 0.2924f : 0.24480002f;
            }
        } else {
            speedToApply = metaSpeedSlider.get();
        }

        if (slownessEffect != null) speedToApply *= 0.835f;
        if (!mc.player.isOnGround()) speedToApply *= 1.435f;

        if (metaDamageBoost.get()) {
            metaDamageUtil.time(metaBoostDuration.get().longValue());
            if (metaDamageUtil.isNormalDamage()) speedToApply += metaBoostSpeed.get() / 10.0f;
        }

        setHorizontalMotion(speedToApply);
    }

    private void handleHolyWorld() {
        if (!MoveUtils.isMoving() || mc.player.isElytraFlying()
                || mc.player.isOnLadder() || mc.player.isInWater() || mc.player.isInLava()) {
            holyBoostTicks = 0;
            return;
        }

        holyBoostTicks++;
        int cycle = HOLY_BURST_ON_TICKS + HOLY_BURST_OFF_TICKS;
        if ((holyBoostTicks % cycle) >= HOLY_BURST_ON_TICKS) return;

        PlayerEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        double radius = holyRadiusSlider.get();
        double radiusSq = radius * radius;

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == null || entity == mc.player || !entity.isAlive()) continue;
            double dx = entity.getPosX() - mc.player.getPosX();
            double dz = entity.getPosZ() - mc.player.getPosZ();
            double distSq = dx * dx + dz * dz;
            if (distSq <= radiusSq && distSq < closestDist) {
                closestDist = distSq;
                closest = entity;
            }
        }

        if (closest == null) return;

        Vector3d self = mc.player.getPositionVec();
        double yaw = Math.atan2(closest.getPosZ() - self.z, closest.getPosX() - self.x) - Math.PI / 2.0;
        double boost = holySpeedSlider.get() / 10.0;

        Vector3d motion = mc.player.getMotion();
        double addX = -Math.sin(yaw) * boost;
        double addZ = Math.cos(yaw) * boost;
        mc.player.setMotion(motion.x + addX, motion.y, motion.z + addZ);
    }

    private boolean isStrafe() {
        return modeSetting.is("Strafe") || modeSetting.is("StrafeStrict");
    }

    private double getPotionSpeed(double speed) {
        if (mc.player.isPotionActive(Effects.SPEED))
            speed *= 1.0 + 0.2 * (mc.player.getActivePotionEffect(Effects.SPEED).getAmplifier() + 1);
        if (mc.player.isPotionActive(Effects.SLOWNESS))
            speed /= 1.0 + 0.2 * (mc.player.getActivePotionEffect(Effects.SLOWNESS).getAmplifier() + 1);
        return speed;
    }

    private double getPotionJump(double jump) {
        if (mc.player.isPotionActive(Effects.JUMP_BOOST))
            jump += (mc.player.getActivePotionEffect(Effects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
        return jump;
    }

    private void setHorizontalMotion(double speed) {
        double forward = mc.player.movementInput.moveForward;
        double strafe = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.rotationYaw;

        if (forward == 0.0 && strafe == 0.0) {
            mc.player.setMotion(0.0, mc.player.getMotion().y, 0.0);
            return;
        }
        if (forward != 0.0) {
            if (strafe > 0.0) yaw += forward > 0.0 ? -45.0f : 45.0f;
            else if (strafe < 0.0) yaw += forward > 0.0 ? 45.0f : -45.0f;
            strafe = 0.0;
            forward = forward > 0.0 ? 1.0 : -1.0;
        }
        if (strafe > 0.0) strafe = 1.0;
        else if (strafe < 0.0) strafe = -1.0;

        double mx = Math.cos(Math.toRadians(yaw + 90.0f));
        double mz = Math.sin(Math.toRadians(yaw + 90.0f));
        mc.player.setMotion(
                forward * speed * mx + strafe * speed * mz,
                mc.player.getMotion().y,
                forward * speed * mz - strafe * speed * mx
        );
    }

    private boolean isMetaStrongItem(String itemName) {
        return itemName.contains("Шар Геракла 2") || itemName.contains("Шар CHAMPION")
                || itemName.contains("Шар GOD") || itemName.contains("Талисман Венома")
                || itemName.contains("КУБИК-РУБИК");
    }
}
