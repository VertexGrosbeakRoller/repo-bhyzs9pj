package copperhead.client.implement.features.modules.movement;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.common.util.entity.InventoryUtils;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ElytraMotion extends Module {
    private boolean lastTick = false;
    private int fireworkTick;

    public ElytraMotion() {
        super("ElytraMotion", "Elytra Motion", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventUpdate e) {
        if (mc.player == null) return;

        int lastSlot = mc.player.inventory.selected;
        if (mc.player.inventory.armor.get(2).getItem() == Items.ELYTRA
                && mc.player.isFallFlying()
                && mc.options.keyUp.isDown()) {
            if (InventoryUtils.boolHotbarItem(Items.FIREWORK_ROCKET)) {
                int fireworkSlot = InventoryUtils.searchHotbarItem(Items.FIREWORK_ROCKET);
                if (fireworkTick >= 10) {
                    lastTick = true;
                    mc.player.inventory.selected = fireworkSlot;
                    mc.gameMode.useItem(mc.player, mc.level, Hand.MAIN_HAND);
                    mc.player.inventory.selected = lastSlot;
                    fireworkTick = 0;
                } else fireworkTick++;
                if (lastTick) {
                    mc.player.setDeltaMovement(0, 0, 0);
                }
            }
        } else {
            fireworkTick = 10;
        }
    }

    @Override
    public void onDisable() {
        fireworkTick = 10;
        super.onDisable();
    }
}
