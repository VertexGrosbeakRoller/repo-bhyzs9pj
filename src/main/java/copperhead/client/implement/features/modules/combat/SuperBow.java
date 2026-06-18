package copperhead.client.implement.features.modules.combat;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.implement.events.EventUpdate;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class SuperBow extends Module {

    private final SliderSetting power = new SliderSetting("Сила", 1.0f, 0.1f, 1.0f, 0.05f);
    private final SliderSetting speedMultiplier = new SliderSetting("Скорость перезарядки", 1.5f, 1.0f, 3.0f, 0.1f);
    private final BooleanSetting autoRecharge = new BooleanSetting("Авто перезарядка", true);
    private final BooleanSetting onlyFullCharge = new BooleanSetting("Только полная зарядка", true);

    private int ticksUsing;
    private boolean released;

    public SuperBow() {
        super("SuperBow", "Super Bow", ModuleCategory.COMBAT);
        addSettings(power, speedMultiplier, autoRecharge, onlyFullCharge);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.level == null) return;

        boolean holdingBow = mc.player.getMainHandItem().getItem() instanceof BowItem
                || mc.player.getMainHandItem().getItem() == Items.BOW;

        if (!holdingBow) {
            ticksUsing = 0;
            released = false;
            return;
        }

        if (mc.player.isUsingItem() && mc.player.getUsedItemHand() == Hand.MAIN_HAND) {
            ticksUsing++;

            int requiredTicks = (int) (20 / speedMultiplier.get());
            float chargePercent = BowItem.getPowerForTime(ticksUsing);

            if (onlyFullCharge.get()) {
                if (chargePercent >= power.get() && !released) {
                    mc.player.connection.send(new CPlayerDiggingPacket(
                            CPlayerDiggingPacket.Action.RELEASE_USE_ITEM,
                            BlockPos.ZERO, Direction.DOWN));
                    mc.player.releaseUsingItem();
                    released = true;

                    if (autoRecharge.get()) {
                        mc.player.connection.send(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                        ticksUsing = 0;
                        released = false;
                    }
                }
            }
        } else {
            ticksUsing = 0;
            released = false;
        }
    }

    @Override
    public boolean onEnable() {
        ticksUsing = 0;
        released = false;
        return super.onEnable();
    }
}
