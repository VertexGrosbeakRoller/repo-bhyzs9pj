package copperhead.client;

import copperhead.client.managers.FriendManager;
import copperhead.client.managers.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("copperhead")
public class CopperHead {
    public static final String NAME = "CopperHead";
    public static final String VERSION = "1.0.0";

    private static CopperHead instance;
    private ModuleManager moduleManager;
    private FriendManager friendManager;

    public CopperHead() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        init();
    }

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
