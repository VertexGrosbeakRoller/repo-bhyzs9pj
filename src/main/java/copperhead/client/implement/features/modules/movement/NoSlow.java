package copperhead.client.implement.features.modules.movement;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.implement.events.EventNoSlow;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class NoSlow extends Module {
    private final ModeSetting mode = new ModeSetting("Мод", "ReallyWorld",
            "ReallyWorld", "Grim", "Matrix", "Обычный", "GrimTick", "LonyGrief");

    private int ticks;

    public NoSlow() {
        super("NoSlow", "No Slow", ModuleCategory.MOVEMENT);
        addSettings(mode);
    }

    @Override
    public boolean onEnable() {
        ticks = 0;
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        ticks = 0;
        super.onDisable();
    }

    @EventHandler
    public void onSlow(EventNoSlow event) {
        if (mc.player == null || mc.player.isFallFlying() || !mc.player.isUsingItem()) {
            return;
        }

        switch (mode.get()) {
            case "ReallyWorld":
                handleReallyWorld(event);
                break;
            case "Grim":
                handleGrim(event);
                break;
            case "Matrix":
                handleMatrix(event);
                break;
            case "GrimTick":
                handleGrimTick(event);
                break;
            case "LonyGrief":
                handleLonyGrief(event);
                break;
            case "Обычный":
                event.cancel();
                break;
        }
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.player.isFallFlying()) return;

        if (mode.is("ReallyWorld")) {
            ticks = mc.player.isUsingItem() ? ticks + 1 : 0;
        }

        if (mode.is("LonyGrief")) {
            handleLonyGriefUpdate();
        }
    }

    private void handleReallyWorld(EventNoSlow event) {
        if (ticks >= 3) {
            event.cancel();
            ticks = 0;
        }
    }

    private void handleGrimTick(EventNoSlow event) {
        if (mc.player.tickCount % 2 == 0 && !mc.player.isShiftKeyDown()) {
            event.cancel();
        }
    }

    private void handleGrim(EventNoSlow event) {
        boolean offHandActive = mc.player.getUsedItemHand() == Hand.OFF_HAND;
        boolean mainHandActive = mc.player.getUsedItemHand() == Hand.MAIN_HAND;

        if (!(mc.player.getUseItemRemainingTicks() < 25 && mc.player.getUseItemRemainingTicks() > 4)
                && mc.player.getOffhandItem().getItem() != Items.SHIELD) {
            return;
        }

        if (!mc.player.isPassenger()) {
            mc.player.connection.send(new CHeldItemChangePacket(mc.player.inventory.selected));

            if (offHandActive && !mc.player.getCooldowns().isOnCooldown(mc.player.getOffhandItem().getItem())) {
                int oldSlot = mc.player.inventory.selected;
                int fakeSlot = oldSlot + 1 > 8 ? oldSlot - 1 : oldSlot + 1;

                mc.player.connection.send(new CHeldItemChangePacket(fakeSlot));
                mc.player.connection.send(new CHeldItemChangePacket(oldSlot));
                mc.player.setSprinting(false);
                event.cancel();
            }

            if (mainHandActive && !mc.player.getCooldowns().isOnCooldown(mc.player.getMainHandItem().getItem())) {
                mc.player.connection.send(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                if (mc.player.getOffhandItem().getItem().getUseAnimation(mc.player.getOffhandItem()) == UseAction.NONE) {
                    event.cancel();
                }
            }

            mc.player.connection.send(new CHeldItemChangePacket(mc.player.inventory.selected));
        }
    }

    private void handleLonyGrief(EventNoSlow event) {
        if (mc.player.getUsedItemHand() == Hand.OFF_HAND) {
            handleGrimTick(event);
            return;
        }

        if (mc.player.getTicksUsingItem() > 0) {
            event.cancel();
        }
    }

    private void handleLonyGriefUpdate() {
        if (mc.player.connection == null) return;

        if (mc.player.isUsingItem() && mc.player.getTicksUsingItem() == 0) {
            mc.player.connection.send(new CPlayerDiggingPacket(
                    CPlayerDiggingPacket.Action.DROP_ALL_ITEMS,
                    BlockPos.ZERO,
                    mc.player.getDirection()
            ));
        }
    }

    private void handleMatrix(EventNoSlow event) {
        boolean falling = mc.player.fallDistance > 0.725f;
        event.cancel();

        if (mc.player.isOnGround() && !mc.player.input.jumping) {
            if (mc.player.tickCount % 2 == 0) {
                float speedMultiplier = mc.player.input.leftImpulse == 0.0f ? 0.5f : 0.4f;
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x * speedMultiplier,
                        mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * speedMultiplier
                );
            }
        } else if (falling) {
            float speedMultiplier = mc.player.fallDistance > 1.4f ? 0.95f : 0.97f;
            mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x * speedMultiplier,
                    mc.player.getDeltaMovement().y,
                    mc.player.getDeltaMovement().z * speedMultiplier
            );
        }
    }
}
