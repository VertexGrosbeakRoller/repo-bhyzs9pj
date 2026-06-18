package copperhead.client.api.feature.setting;

public class ModeSetting extends Setting<String> {
    private final String[] modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = modes;
    }

    public boolean is(String mode) {
        return get().equalsIgnoreCase(mode);
    }

    public String[] getModes() { return modes; }

    public int getIndex() {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equalsIgnoreCase(get())) return i;
        }
        return 0;
    }
}
