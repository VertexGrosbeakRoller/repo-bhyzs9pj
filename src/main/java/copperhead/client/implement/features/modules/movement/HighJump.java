package copperhead.client.implement.features.modules.movement;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.entity.InventoryUtils;
import copperhead.client.implement.events.EventFireworkRocket;
import copperhead.client.implement.events.EventMotion;
import copperhead.client.implement.events.EventPacket;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;

public class HighJump extends Module {
    private final ModeSetting mode = new ModeSetting("Режим", "Elytra", "Elytra", "Shulker", "Elytra2");
    private final SliderSetting jumpBoost = new SliderSetting("Высота прыжка", 0.2f, 0.1f, 2.0f, 0.1f);
    private final BooleanSetting autoClose = new BooleanSetting("Автоматически закрывать шалкер", true)
            .setVisible(() -> mode.is("Shulker"));
    private final BooleanSetting autoSwap = new BooleanSetting("Умный свап", false)
            .setVisible(() -> mode.is("Elytra2"));

    private boolean hasFiredOnStart = false;

    public HighJump() {
        super("HighJump", "High Jump", ModuleCategory.MOVEMENT);
        addSettings(mode, jumpBoost, autoClose, autoSwap);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (mode.is("Elytra")) {
            handleElytraMode();
        } else if (mode.is("Elytra2")) {
            handleElytra2Mode();
        }
    }

    private void handleElytraMode() {
        if (mc.player.isAlive() && mc.player.isElytraFlying()) {
            mc.gameSettings.keyBindForward.setPressed(false);
            mc.gameSettings.keyBindRight.setPressed(false);
            mc.gameSettings.keyBindBack.setPressed(false);
            mc.gameSettings.keyBindLeft.setPressed(false);
            mc.gameSettings.keyBindJump.setPressed(false);
            mc.gameSettings.keyBindSneak.setPressed(false);
            mc.player.setMotion(0, jumpBoost.get(), 0);
        }
    }

    private void handleElytra2Mode() {
        if (!mc.player.abilities.isFlying
                && mc.player.isOnGround()
                && !mc.player.isInWater()
                && !mc.player.isInLava()
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA
                && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.jump();
        }

        if (!mc.player.abilities.isFlying
                && !mc.player.isOnGround()
                && !mc.player.isInWater()
                && !mc.player.isElytraFlying()
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            mc.player.startFallFlying();
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
        }

        if (mc.player.isOnGround() || mc.player.isInWater() || mc.player.isInLava()) {
            hasFiredOnStart = false;
        }

        if (mc.player.hurtTime > 0 && autoSwap.get()
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            swapToChestplate();
            return;
        }

        if (mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            mc.gameSettings.keyBindJump.setPressed(true);
            if (mc.player.isElytraFlying()) {
                mc.player.getMotion().add(0, jumpBoost.get() / 10, 0);
            }
        } else if (autoSwap.get()) {
            swapToElytra();
        }
    }

    @EventHandler
    public void onMotion(EventMotion e) {
        if (mode.is("Elytra") && mc.player != null && mc.player.isElytraFlying()) {
            mc.player.rotationPitch = 0.0F;
            mc.player.rotationYaw = mc.player.rotationYaw;
        } else if (mode.is("Elytra2") && mc.player != null && mc.player.isElytraFlying()) {
            float targetYaw = mc.player.rotationYaw;
            e.setYaw(targetYaw);
            e.setPitch(0.0f);
            mc.player.rotationPitch = 0.0f;
        }
    }

    @EventHandler
    public void onPacket(EventPacket e) {
        if (mode.is("Elytra") && e.isReceive()) {
            if (e.getPacket() instanceof SEntityMetadataPacket) {
                SEntityMetadataPacket packet = (SEntityMetadataPacket) e.getPacket();
                if (mc.player != null && packet.getEntityId() == mc.player.getEntityId() && !mc.player.isElytraFlying()) {
                    e.cancel();
                }
            }
        }
    }

    @EventHandler
    public void onFireworkRocket(EventFireworkRocket event) {
        if (mode.is("Elytra2")) {
            event.cancel();
        }
    }

    @Override
    public boolean onEnable() {
        hasFiredOnStart = false;
        if (mode.is("Elytra2") && autoSwap.get()) {
            swapToElytra();
        }
        return super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mode.is("Elytra2") && autoSwap.get() && mc.player != null
                && mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem() == Items.ELYTRA) {
            swapToChestplate();
        }
        super.onDisable();
    }

    private void swapToElytra() {
        int slot = getItemSlot(Items.ELYTRA);
        if (slot != -1) {
            InventoryUtils.moveItem(slot, 6);
        }
    }

    private void swapToChestplate() {
        int slot = getChestPlateSlot();
        if (slot != -1) {
            InventoryUtils.moveItem(slot, 6);
        }
    }

    private int getItemSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                finalSlot = i;
                break;
            }
        }
        if (finalSlot < 9 && finalSlot != -1) finalSlot += 36;
        return finalSlot;
    }

    private int getChestPlateSlot() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.GOLDEN_CHESTPLATE,
                Items.IRON_CHESTPLATE, Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE};
        for (Item item : items) {
            for (int i = 0; i < 36; ++i) {
                if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }
        return -1;
    }
}
