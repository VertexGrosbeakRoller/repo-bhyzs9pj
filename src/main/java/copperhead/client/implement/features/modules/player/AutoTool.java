package copperhead.client.implement.features.modules.player;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import java.util.List;

public class AutoTool extends Module {

    private final BooleanSetting allItems = new BooleanSetting("Все предметы", true);
    private final BooleanSetting inventorySwap = new BooleanSetting("Свап из инвентаря", true);
    private final BooleanSetting autoPotion = new BooleanSetting("Авто зелье (ПКМ)", false);
    private final BooleanSetting onlyPositiveEffects = new BooleanSetting("Только хорошие зелья", true)
            .setVisible(autoPotion::get);
    private final BooleanSetting silkTouch = new BooleanSetting("Приоритет Шёлковое касание", false);
    private final BooleanSetting fortune = new BooleanSetting("Приоритет Удача", false);
    private final BooleanSetting switchBack = new BooleanSetting("Вернуть слот", true);
    private final SliderSetting switchBackDelay = new SliderSetting("Задержка возврата (мс)", 500f, 100f, 2000f, 50f)
            .setVisible(switchBack::get);

    private int previousSlot = -1;
    private long breakStartTime = 0;
    private boolean wasBreaking = false;

    public AutoTool() {
        super("AutoTool", "Auto Tool", ModuleCategory.PLAYER);
        addSettings(allItems, inventorySwap, autoPotion, onlyPositiveEffects,
                silkTouch, fortune, switchBack, switchBackDelay);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (autoPotion.get() && mc.options.keyUse.isDown()) {
            handleAutoPotion();
        }

        if (mc.options.keyAttack.isDown()
                && mc.hitResult != null
                && mc.hitResult.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockResult = (BlockRayTraceResult) mc.hitResult;
            BlockState state = mc.level.getBlockState(blockResult.getBlockPos());

            if (!wasBreaking) {
                previousSlot = mc.player.inventory.selected;
                breakStartTime = System.currentTimeMillis();
                wasBreaking = true;
            }

            int bestSlot = findBestTool(state);
            if (bestSlot != -1) {
                mc.player.inventory.selected = bestSlot;
            }
        } else {
            if (wasBreaking && switchBack.get() && previousSlot != -1) {
                long elapsed = System.currentTimeMillis() - breakStartTime;
                if (elapsed >= (long) switchBackDelay.get().floatValue()) {
                    mc.player.inventory.selected = previousSlot;
                    previousSlot = -1;
                }
            }
            wasBreaking = false;
        }
    }

    private int findBestTool(BlockState state) {
        float bestSpeed = 1.0f;
        int bestSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getItem(i);
            float speed = getDestroySpeed(stack, state);
            if (speed > bestSpeed || (speed == bestSpeed && hasPriorityEnchant(stack))) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        if (inventorySwap.get() && bestSlot == -1) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.inventory.getItem(i);
                float speed = getDestroySpeed(stack, state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    int targetHotbar = mc.player.inventory.selected;
                    mc.gameMode.handleInventoryMouseClick(0, i, targetHotbar,
                            ClickType.SWAP, mc.player);
                    return targetHotbar;
                }
            }
        }

        return bestSlot;
    }

    private float getDestroySpeed(ItemStack stack, BlockState state) {
        if (stack.isEmpty()) return 1.0f;

        float speed = stack.getDestroySpeed(state);

        if (!allItems.get() && !(stack.getItem() instanceof ToolItem)
                && !(stack.getItem() instanceof ShearsItem)) {
            return 1.0f;
        }

        int efficiency = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, stack);
        if (efficiency > 0 && speed > 1.0f) {
            speed += efficiency * efficiency + 1;
        }

        return speed;
    }

    private boolean hasPriorityEnchant(ItemStack stack) {
        if (silkTouch.get() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            return true;
        }
        if (fortune.get() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack) > 0) {
            return true;
        }
        return false;
    }

    private void handleAutoPotion() {
        if (mc.player == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getItem(i);
            if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof SplashPotionItem) {
                List<EffectInstance> effects = PotionUtils.getMobEffects(stack);
                boolean allPositive = effects.stream().allMatch(eff ->
                        eff.getEffect().getCategory() == EffectType.BENEFICIAL);

                if (onlyPositiveEffects.get() && !allPositive) continue;

                boolean alreadyHas = effects.stream().allMatch(eff ->
                        mc.player.hasEffect(eff.getEffect()));
                if (alreadyHas) continue;

                int prevSlot = mc.player.inventory.selected;
                mc.player.inventory.selected = i;
                mc.gameMode.useItem(mc.player, mc.level, Hand.MAIN_HAND);
                mc.player.inventory.selected = prevSlot;
                break;
            }
        }

        if (inventorySwap.get()) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.inventory.getItem(i);
                if (stack.getItem() instanceof SplashPotionItem) {
                    List<EffectInstance> effects = PotionUtils.getMobEffects(stack);
                    boolean allPositive = effects.stream().allMatch(eff ->
                            eff.getEffect().getCategory() == EffectType.BENEFICIAL);
                    if (onlyPositiveEffects.get() && !allPositive) continue;

                    boolean alreadyHas = effects.stream().allMatch(eff ->
                            mc.player.hasEffect(eff.getEffect()));
                    if (alreadyHas) continue;

                    int targetSlot = mc.player.inventory.selected;
                    mc.gameMode.handleInventoryMouseClick(0, i, targetSlot, ClickType.SWAP, mc.player);
                    mc.gameMode.useItem(mc.player, mc.level, Hand.MAIN_HAND);
                    mc.gameMode.handleInventoryMouseClick(0, i, targetSlot, ClickType.SWAP, mc.player);
                    break;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (previousSlot != -1 && mc.player != null) {
            mc.player.inventory.selected = previousSlot;
            previousSlot = -1;
        }
        wasBreaking = false;
        super.onDisable();
    }
}
