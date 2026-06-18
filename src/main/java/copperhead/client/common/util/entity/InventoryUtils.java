package copperhead.client.common.util.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public final class InventoryUtils {
    public static final String HAND_SETTING_NAME = "Рука";
    public static final String RIGHT_HAND_MODE = "Правая";
    public static final String LEFT_HAND_MODE = "Левая";

    private static final Minecraft mc = Minecraft.getInstance();

    private InventoryUtils() {}

    public static boolean hasItem(Item item) {
        if (mc.player == null) return false;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getItem(i).getItem() == item) return true;
        }
        return false;
    }

    public static int searchHotbarItem(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.inventory.getItem(i).getItem() == item) return i;
        }
        return -1;
    }

    public static boolean boolHotbarItem(Item item) {
        return searchHotbarItem(item) != -1;
    }

    public static void inventorySwapClick(Item item, boolean offhand) {
        // Swap item to hotbar and use
        if (mc.player == null) return;
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getItem(i).getItem() == item) {
                slot = i;
                break;
            }
        }
        if (slot != -1) {
            int lastSlot = mc.player.inventory.selected;
            mc.player.inventory.selected = slot < 9 ? slot : lastSlot;
            mc.player.inventory.selected = lastSlot;
        }
    }

    public static void moveItem(int from, int to) {
        if (mc.player == null || mc.player.containerMenu == null) return;
        mc.gameMode.handleInventoryMouseClick(0, from, 0, net.minecraft.inventory.container.ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(0, to, 0, net.minecraft.inventory.container.ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(0, from, 0, net.minecraft.inventory.container.ClickType.PICKUP, mc.player);
    }

    public static void finalizeUse() {
        // placeholder for use finalization
    }

    public static int getSlotInInventory(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getItem(i).getItem() == item) return i;
        }
        return -1;
    }
}
