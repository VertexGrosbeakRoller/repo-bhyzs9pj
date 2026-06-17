package copperhead.client.implement.features.modules.combat;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.ModeSetting;
import copperhead.client.implement.events.EventUpdate;
import copperhead.client.implement.events.EventWorldChange;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;

import java.util.*;

public class AntiBot extends Module {
    private final ModeSetting mode = new ModeSetting("Обход", "ReallyWorld", "ReallyWorld", "Matrix", "UniAC");
    private final BooleanSetting removeFromWorld = new BooleanSetting("Удалять из мира", false)
            .setVisible(() -> mode.is("UniAC"));

    public static final List<Entity> bot = new ArrayList<>();
    private final Set<Integer> hiddenBotIds = new HashSet<>();

    public AntiBot() {
        super("AntiBot", "Anti Bot", ModuleCategory.COMBAT);
        addSettings(mode, removeFromWorld);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (mode.is("UniAC") && removeFromWorld.get()) {
            for (Entity entity : new ArrayList<>(bot)) {
                if (entity instanceof PlayerEntity && hiddenBotIds.contains(entity.getEntityId())) {
                    mc.world.removeEntityFromWorld(entity.getEntityId());
                }
            }
        } else {
            hiddenBotIds.clear();
        }

        for (Entity entity : mc.world.getAllEntities()) {
            if (!(entity instanceof PlayerEntity) || entity.equals(mc.player)) continue;
            PlayerEntity player = (PlayerEntity) entity;
            boolean isBot = false;

            if (mode.is("ReallyWorld")) {
                boolean hasFullFood = player.getFoodStats().getFoodLevel() == 20;
                boolean hasValidArmor = player.inventory.armorInventory.stream()
                        .allMatch(armorItem -> armorItem.getItem() != Items.AIR
                                && armorItem.isEnchantable() && !armorItem.isDamaged());
                boolean hasValidEquipment = player.getHeldItemOffhand().getItem() == Items.AIR
                        && player.inventory.armorInventory.stream().anyMatch(armorItem ->
                        armorItem.getItem() == Items.LEATHER_BOOTS
                                || armorItem.getItem() == Items.LEATHER_LEGGINGS
                                || armorItem.getItem() == Items.LEATHER_CHESTPLATE
                                || armorItem.getItem() == Items.LEATHER_HELMET
                                || armorItem.getItem() == Items.IRON_BOOTS
                                || armorItem.getItem() == Items.IRON_LEGGINGS
                                || armorItem.getItem() == Items.IRON_CHESTPLATE
                                || armorItem.getItem() == Items.IRON_HELMET);
                isBot = hasValidArmor && hasValidEquipment && hasFullFood;
            } else if (mode.is("Matrix")) {
                isBot = player.isAlive() && !bot.contains(player)
                        && !player.getUniqueID().equals(PlayerEntity.getOfflineUUID(player.getName().getString()));
            } else if (mode.is("UniAC")) {
                isBot = isUniACBot(player);
            }

            if (isBot) {
                if (!bot.contains(player)) {
                    bot.add(player);
                    if (mode.is("UniAC") && removeFromWorld.get()) {
                        hiddenBotIds.add(player.getEntityId());
                    }
                }
            } else {
                bot.remove(player);
                hiddenBotIds.remove(player.getEntityId());
            }
        }
    }

    private boolean isUniACBot(PlayerEntity player) {
        String name = player.getName().getString();
        if (mc.getConnection() == null) return true;
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
            if (info.getGameProfile().getName().equals(name)) {
                ITextComponent display = info.getDisplayName();
                return display == null || display.getString().equals(name);
            }
        }
        return true;
    }

    @EventHandler
    public void onWorldChange(EventWorldChange e) {
        bot.clear();
        hiddenBotIds.clear();
    }

    @Override
    public void onDisable() {
        bot.clear();
        hiddenBotIds.clear();
        super.onDisable();
    }
}
