package baritone.api;

public final class BaritoneAPI {
    private static IBaritone instance;
    private static final Settings settings = new Settings();

    private BaritoneAPI() {}

    public static void setInstance(IBaritone baritone) {
        instance = baritone;
    }

    public static IBaritone getProvider() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }
}
