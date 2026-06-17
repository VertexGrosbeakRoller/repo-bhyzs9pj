package copperhead.client.implement.features.modules.misc;

import copperhead.client.api.event.EventHandler;
import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.BooleanSetting;
import copperhead.client.api.feature.setting.SliderSetting;
import copperhead.client.api.feature.setting.StringSetting;
import copperhead.client.implement.events.EventUpdate;
import copperhead.client.managers.FriendManager;

import java.util.List;

public class FriendCord extends Module {

    private final SliderSetting interval = new SliderSetting("Интервал (сек)", 10f, 1f, 60f, 1f);
    private final BooleanSetting includeY = new BooleanSetting("Включить Y", true);
    private final BooleanSetting roundCoords = new BooleanSetting("Округлять", true);
    private final StringSetting prefix = new StringSetting("Префикс", "Мои координаты:");

    private long lastSendTime = 0;

    public FriendCord() {
        super("FriendCord", "Friend Cord", ModuleCategory.MISC);
        addSettings(interval, includeY, roundCoords, prefix);
    }

    @EventHandler
    public void onUpdate(EventUpdate e) {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastSendTime < interval.get() * 1000) return;
        lastSendTime = now;

        List<String> friends = FriendManager.getFriends();
        if (friends.isEmpty()) return;

        double x = mc.player.getPosX();
        double y = mc.player.getPosY();
        double z = mc.player.getPosZ();

        String coords;
        if (roundCoords.get()) {
            if (includeY.get()) {
                coords = String.format("%s X: %d Y: %d Z: %d", prefix.get(),
                        (int) x, (int) y, (int) z);
            } else {
                coords = String.format("%s X: %d Z: %d", prefix.get(),
                        (int) x, (int) z);
            }
        } else {
            if (includeY.get()) {
                coords = String.format("%s X: %.1f Y: %.1f Z: %.1f", prefix.get(), x, y, z);
            } else {
                coords = String.format("%s X: %.1f Z: %.1f", prefix.get(), x, z);
            }
        }

        for (String friend : friends) {
            mc.player.sendChatMessage("/msg " + friend + " " + coords);
        }
    }

    @Override
    public boolean onEnable() {
        lastSendTime = 0;
        return super.onEnable();
    }
}
