package copperhead.client.api.feature.module;

import copperhead.client.api.event.EventBus;
import copperhead.client.api.feature.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Module {
    protected static final Minecraft mc = Minecraft.getInstance();

    private final String name;
    private final String displayName;
    private final ModuleCategory category;
    private boolean enabled;
    private int keyBind = -1;
    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String displayName, ModuleCategory category) {
        this.name = name;
        this.displayName = displayName;
        this.category = category;
    }

    public Module(String name, ModuleCategory category) {
        this(name, name, category);
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public ModuleCategory getCategory() { return category; }
    public List<Setting<?>> getSettings() { return settings; }
    public boolean isState() { return enabled; }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int keyBind) { this.keyBind = keyBind; }

    public void toggle() {
        if (enabled) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        this.enabled = true;
        onEnable();
        EventBus.getInstance().register(this);
    }

    public void disable() {
        this.enabled = false;
        EventBus.getInstance().unregister(this);
        onDisable();
    }

    public boolean onEnable() { return true; }
    public void onDisable() {}
}
