package copperhead.client;

import copperhead.client.managers.FriendManager;
import copperhead.client.managers.ModuleManager;
import net.minecraft.client.Minecraft;

public class CopperHead {
    public static final String NAME = "CopperHead";
    public static final String VERSION = "1.0.0";

    private static CopperHead instance;
    private ModuleManager moduleManager;
    private FriendManager friendManager;

    public static CopperHead getInstance() {
        if (instance == null) {
            instance = new CopperHead();
        }
        return instance;
    }

    public void init() {
        friendManager = new FriendManager();
        moduleManager = new ModuleManager();
        moduleManager.init();
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }
}
