package copperhead.client.implement.features.modules.player;

import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.api.feature.setting.StringSetting;

public class NameProtect extends Module {

    private final StringSetting replaceName = new StringSetting("Заменить на", "copperhead.fun");

    public NameProtect() {
        super("NameProtect", "Name Protect", ModuleCategory.PLAYER);
        addSettings(replaceName);
    }

    public String getProtectedName(String original) {
        if (!isState() || mc.player == null) return original;
        if (original.equals(mc.player.getName().getString())) {
            return replaceName.get();
        }
        return original;
    }

    public String getReplaceName() {
        return replaceName.get();
    }
}
