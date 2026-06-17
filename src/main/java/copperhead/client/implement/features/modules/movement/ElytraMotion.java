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

        int lastSlot = mc.player.inventory.currentItem;
        if (mc.player.inventory.armorInventory.get(2).getItem() == Items.ELYTRA
                && mc.player.isElytraFlying()
                && mc.gameSettings.keyBindForward.isKeyDown()) {
            if (InventoryUtils.boolHotbarItem(Items.FIREWORK_ROCKET)) {
                int fireworkSlot = InventoryUtils.searchHotbarItem(Items.FIREWORK_ROCKET);
                if (fireworkTick >= 10) {
                    lastTick = true;
                    mc.player.inventory.currentItem = fireworkSlot;
                    mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
                    mc.player.inventory.currentItem = lastSlot;
                    fireworkTick = 0;
                } else fireworkTick++;
                if (lastTick) {
                    mc.player.setMotion(0, 0, 0);
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
