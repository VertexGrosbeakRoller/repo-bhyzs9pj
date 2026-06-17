package copperhead.client.implement.features.modules.combat;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.implement.events.EventPacket;
import copperhead.client.implement.events.EventThorns;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;

public class AntiThorns extends Module {
    private static final int ELYTRA_THORNS_VELOCITY_TICKS = 8;
    private static final byte THORNS_STATUS = 33;

    private int elytraThornsVelocityTicks;

    public AntiThorns() {
        super("AntiThorns", "Anti Thorns", ModuleCategory.COMBAT);
    }

    @EventHandler
    public void onThorns(EventThorns event) {
        if (mc.player == null || event.getAttacker() != mc.player) {
            return;
        }

        if (mc.player.isElytraFlying()) {
            elytraThornsVelocityTicks = ELYTRA_THORNS_VELOCITY_TICKS;
        }

        event.cancel();
    }

    @EventHandler
    public void onPacket(EventPacket event) {
        if (mc.player == null || mc.world == null) {
            elytraThornsVelocityTicks = 0;
            return;
        }

        if (!event.isReceive()) {
            return;
        }

        if (event.getPacket() instanceof SEntityStatusPacket) {
            SEntityStatusPacket packet = (SEntityStatusPacket) event.getPacket();
            if (packet.getOpCode() == THORNS_STATUS
                    && packet.getEntity(mc.world) == mc.player
                    && mc.player.isElytraFlying()) {
                elytraThornsVelocityTicks = ELYTRA_THORNS_VELOCITY_TICKS;
                return;
            }
        }

        if (event.getPacket() instanceof SEntityVelocityPacket) {
            SEntityVelocityPacket packet = (SEntityVelocityPacket) event.getPacket();
            if (packet.getEntityID() == mc.player.getEntityId()
                    && shouldCancelElytraThornsVelocity()) {
                event.cancel();
                elytraThornsVelocityTicks = 0;
            }
        }
    }

    @EventHandler
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || !mc.player.isElytraFlying()) {
            elytraThornsVelocityTicks = 0;
            return;
        }

        if (elytraThornsVelocityTicks > 0) {
            elytraThornsVelocityTicks--;
        }
    }

    @Override
    public boolean onEnable() {
        elytraThornsVelocityTicks = 0;
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        elytraThornsVelocityTicks = 0;
        super.onDisable();
    }

    private boolean shouldCancelElytraThornsVelocity() {
        return elytraThornsVelocityTicks > 0 && mc.player.isElytraFlying();
    }
}
