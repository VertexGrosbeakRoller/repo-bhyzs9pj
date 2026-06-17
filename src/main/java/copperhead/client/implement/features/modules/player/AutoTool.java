package copperhead.client.implement.features.modules.player;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.common.util.entity.InventoryUtils;
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

/**
 * AutoTool — автоматически выбирает лучший инструмент.
 * Поддержка: все предметы, свап из инвентаря, авто-зелье по ПКМ.
 */
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
        if (mc.player == null || mc.world == null || mc.playerController == null) return;

        // Auto potion on right-click
        if (autoPotion.get() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            handleAutoPotion();
        }

        // Auto tool on block break
        if (mc.gameSettings.keyBindAttack.isKeyDown()
                && mc.objectMouseOver != null
                && mc.objectMouseOver.getType() == RayTraceResult.Type.BLOCK) {
            BlockRayTraceResult blockResult = (BlockRayTraceResult) mc.objectMouseOver;
            BlockState state = mc.world.getBlockState(blockResult.getPos());

            if (!wasBreaking) {
                previousSlot = mc.player.inventory.currentItem;
                breakStartTime = System.currentTimeMillis();
                wasBreaking = true;
            }

            int bestSlot = findBestTool(state);
            if (bestSlot != -1) {
                mc.player.inventory.currentItem = bestSlot;
            }
        } else {
            if (wasBreaking && switchBack.get() && previousSlot != -1) {
                long elapsed = System.currentTimeMillis() - breakStartTime;
                if (elapsed >= switchBackDelay.get()) {
                    mc.player.inventory.currentItem = previousSlot;
                    previousSlot = -1;
                }
            }
            wasBreaking = false;
        }
    }

    private int findBestTool(BlockState state) {
        float bestSpeed = 1.0f;
        int bestSlot = -1;

        // Check hotbar first
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            float speed = getDestroySpeed(stack, state);
            if (speed > bestSpeed || (speed == bestSpeed && hasPriorityEnchant(stack))) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        // Check inventory if enabled and no good tool in hotbar
        if (inventorySwap.get() && bestSlot == -1) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                float speed = getDestroySpeed(stack, state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    // Move to hotbar via inventory click
                    int targetHotbar = mc.player.inventory.currentItem;
                    mc.playerController.windowClick(0, i, targetHotbar,
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

        // All items mode — consider any item
        if (!allItems.get() && !(stack.getItem() instanceof ToolItem)
                && !(stack.getItem() instanceof ShearsItem)) {
            return 1.0f;
        }

        // Efficiency enchantment
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        if (efficiency > 0 && speed > 1.0f) {
            speed += efficiency * efficiency + 1;
        }

        return speed;
    }

    private boolean hasPriorityEnchant(ItemStack stack) {
        if (silkTouch.get() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0) {
            return true;
        }
        if (fortune.get() && EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack) > 0) {
            return true;
        }
        return false;
    }

    private void handleAutoPotion() {
        if (mc.player == null) return;

        // Look for potions in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof PotionItem || stack.getItem() instanceof SplashPotionItem) {
                List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
                boolean allPositive = effects.stream().allMatch(eff ->
                        eff.getPotion().getEffectType() == EffectType.BENEFICIAL);

                if (onlyPositiveEffects.get() && !allPositive) continue;

                // Check if we already have these effects
                boolean alreadyHas = effects.stream().allMatch(eff ->
                        mc.player.isPotionActive(eff.getPotion()));
                if (alreadyHas) continue;

                int prevSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = i;
                mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
                mc.player.inventory.currentItem = prevSlot;
                break;
            }
        }

        // Check inventory for potions if inventory swap is enabled
        if (inventorySwap.get()) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.getItem() instanceof SplashPotionItem) {
                    List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
                    boolean allPositive = effects.stream().allMatch(eff ->
                            eff.getPotion().getEffectType() == EffectType.BENEFICIAL);
                    if (onlyPositiveEffects.get() && !allPositive) continue;

                    boolean alreadyHas = effects.stream().allMatch(eff ->
                            mc.player.isPotionActive(eff.getPotion()));
                    if (alreadyHas) continue;

                    // Swap to hotbar
                    int targetSlot = mc.player.inventory.currentItem;
                    mc.playerController.windowClick(0, i, targetSlot, ClickType.SWAP, mc.player);
                    mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
                    mc.playerController.windowClick(0, i, targetSlot, ClickType.SWAP, mc.player);
                    break;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (previousSlot != -1 && mc.player != null) {
            mc.player.inventory.currentItem = previousSlot;
            previousSlot = -1;
        }
        wasBreaking = false;
        super.onDisable();
    }
}
