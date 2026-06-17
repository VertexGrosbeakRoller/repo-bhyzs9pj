package copperhead.client.api.feature.setting;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public boolean is(String name) {
        return getName().equals(name) && get();
    }
}
