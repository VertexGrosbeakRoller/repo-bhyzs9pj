package copperhead.client.implement.features.modules.movement;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ButtonSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.math.MathUtils;
import copperhead.client.implement.events.EventMove;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.concurrent.ThreadLocalRandom;

public class GrimGlide extends Module {
    private final ModeSetting serverMode = new ModeSetting("Сервер", "ReallyWorld", "ReallyWorld", "LonyGrief");

    private final ButtonSetting rwPreset = new ButtonSetting("Пресет", "Best")
            .addButton("Best", this::applyReallyWorldBestPreset)
            .setVisible(() -> serverMode.is("ReallyWorld"));

    private final ButtonSetting lgPreset = new ButtonSetting("Пресет", "Best")
            .addButton("Best", this::applyLonyGriefBestPreset)
            .setVisible(() -> serverMode.is("LonyGrief"));

    private final SliderSetting ticks = new SliderSetting("Тики", 40F, 1F, 200F, 1F)
            .setVisible(() -> serverMode.is("ReallyWorld"));

    // Настройка подъёма вверх
    private final BooleanSetting allowAscend = new BooleanSetting("Подъём вверх", true);
    private final SliderSetting ascendSpeed = new SliderSetting("Скорость подъёма", 0.05f, 0.01f, 0.2f, 0.005f)
            .setVisible(allowAscend::get);

    private final SliderSetting lgForwardSpeed1 = new SliderSetting("Forward Speed 1", 0.095F, 0.05F, 0.15F, 0.001F)
            .setVisible(() -> serverMode.is("LonyGrief"));
    private final SliderSetting lgForwardSpeed2 = new SliderSetting("Forward Speed 2", 0.105F, 0.05F, 0.15F, 0.001F)
            .setVisible(() -> serverMode.is("LonyGrief"));
    private final SliderSetting lgUpdateDelay = new SliderSetting("Update Delay (ms)", 15F, 5F, 50F, 1F)
            .setVisible(() -> serverMode.is("LonyGrief"));
    private final SliderSetting lgBoostFrequency = new SliderSetting("Boost Frequency", 5F, 1F, 20F, 1F)
            .setVisible(() -> serverMode.is("LonyGrief"));
    private final SliderSetting lgBoostMultMin = new SliderSetting("Boost Multi Min", 1.005F, 1.0F, 1.05F, 0.001F)
            .setVisible(() -> serverMode.is("LonyGrief"));
    private final SliderSetting lgBoostMultMax = new SliderSetting("Boost Multi Max", 1.015F, 1.0F, 1.05F, 0.001F)
            .setVisible(() -> serverMode.is("LonyGrief"));
    private final SliderSetting lgVerticalBoost = new SliderSetting("Vertical Boost", 0.0085F, 0.0F, 0.02F, 0.0001F)
            .setVisible(() -> serverMode.is("LonyGrief"));

    private long lastTickTime = 0;
    private int ticksTwo = 0;

    public GrimGlide() {
        super("GrimGlide", "Grim Glide", ModuleCategory.MOVEMENT);
        addSettings(serverMode, rwPreset, ticks, allowAscend, ascendSpeed,
                lgPreset, lgForwardSpeed1, lgForwardSpeed2, lgUpdateDelay,
                lgBoostFrequency, lgBoostMultMin, lgBoostMultMax, lgVerticalBoost);
    }

    @EventHandler
    public void onEvent(EventMove event) {
        if (serverMode.is("ReallyWorld")) {
            handleReallyWorld(event);
        } else if (serverMode.is("LonyGrief")) {
            handleLonyGrief(event);
        }
    }

    private void handleReallyWorld(EventMove event) {
        if (mc.player == null || mc.world == null || !mc.player.isElytraFlying() || isBoostedByFirework())
            return;
        if (mc.player.isInWater()) return;
        Vector3d pos = mc.player.getPositionVec();

        float yaw = mc.player.rotationYaw;
        double forward = 0.087;
        double motion = MathUtils.getBps(mc.player, 1);

        if (motion >= ticks.get()) {
            forward = 0f;
        }

        double dx = -Math.sin(Math.toRadians(yaw)) * forward;
        double dz = Math.cos(Math.toRadians(yaw)) * forward;

        double yMotion = mc.player.getMotion().y;

        // Подъём вверх при нажатии прыжка
        if (allowAscend.get() && mc.gameSettings.keyBindJump.isKeyDown()) {
            yMotion = ascendSpeed.get();
        }

        mc.player.setVelocity(
                dx * MathUtils.random1(2.5f, 2.71f),
                yMotion,
                dz * MathUtils.random(2.5f, 2.71f)
        );

        mc.player.setPosition(pos.getX() + dx, pos.getY(), pos.getZ() + dz);

        mc.player.setVelocity(
                dx * MathUtils.random1(2.5f, 2.71f),
                yMotion,
                dz * MathUtils.random(2.5f, 2.71f)
        );
    }

    private void handleLonyGrief(EventMove event) {
        if (mc.player == null || mc.world == null || !mc.player.isElytraFlying()) return;

        ticksTwo++;
        Vector3d pos = mc.player.getPositionVec();
        float yaw = mc.player.rotationYaw;
        double forward = mc.player.ticksExisted % 2 == 0 ? lgForwardSpeed1.get() : lgForwardSpeed2.get();

        double dx = -Math.sin(Math.toRadians(yaw)) * forward;
        double dz = Math.cos(Math.toRadians(yaw)) * forward;

        if (System.currentTimeMillis() - lastTickTime >= lgUpdateDelay.get().longValue() - 4) {
            mc.player.setPosition(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
            lastTickTime = System.currentTimeMillis();
        }

        double yBoost = lgVerticalBoost.get();
        // Подъём вверх при нажатии прыжка
        if (allowAscend.get() && mc.gameSettings.keyBindJump.isKeyDown()) {
            yBoost = ascendSpeed.get();
        }

        if (ticksTwo % lgBoostFrequency.get().intValue() == 0) {
            mc.player.setVelocity(
                    dx * ThreadLocalRandom.current().nextFloat() * (lgBoostMultMax.get() - lgBoostMultMin.get()) + lgBoostMultMin.get(),
                    mc.player.getMotion().y + yBoost,
                    dz * ThreadLocalRandom.current().nextFloat() * (lgBoostMultMax.get() - lgBoostMultMin.get()) + lgBoostMultMin.get()
            );
        }
    }

    private boolean isBoostedByFirework() {
        return !mc.world.getEntitiesWithinAABB(
                FireworkRocketEntity.class,
                mc.player.getBoundingBox().grow(5.0D),
                firework -> firework.isAlive()
        ).isEmpty();
    }

    @Override
    public boolean onEnable() {
        ticksTwo = 0;
        lastTickTime = System.currentTimeMillis();
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        ticksTwo = 0;
        super.onDisable();
    }

    private void applyReallyWorldBestPreset() {
        ticks.set(60F);
    }

    private void applyLonyGriefBestPreset() {
        lgForwardSpeed1.set(0.09F);
        lgForwardSpeed2.set(0.09F);
        lgUpdateDelay.set(5F);
        lgBoostFrequency.set(17F);
        lgBoostMultMin.set(1.0F);
        lgBoostMultMax.set(1.0F);
        lgVerticalBoost.set(0.02F);
    }
}
