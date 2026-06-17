package copperhead.client.managers;

import copperhead.client.api.feature.module.Module;
import copperhead.client.api.feature.module.ModuleCategory;
import copperhead.client.implement.features.modules.combat.*;
import copperhead.client.implement.features.modules.misc.*;
import copperhead.client.implement.features.modules.movement.*;
import copperhead.client.implement.features.modules.player.*;
import copperhead.client.implement.features.modules.render.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // Combat
        register(new Aura());
        register(new SuperBow());
        register(new AntiThorns());
        register(new AntiBot());
        register(new Snap());

        // Movement
        register(new Speed());
        register(new NoSlow());
        register(new GrimGlide());
        register(new ElytraMotion());
        register(new HighJump());

        // Render
        register(new Removals());
        register(new TargetESP());
        register(new Cosmetics());
        register(new NameTags());
        register(new Ambience());
        register(new Hands());
        register(new AncientDebris());
        register(new FireFlies());
        register(new Particle());

        // Player
        register(new NameProtect());
        register(new AutoTool());

        // Misc
        register(new FriendCord());
        register(new ServerHelper());
        register(new PlayerSounds());
    }

    private void register(Module module) {
        modules.add(module);
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) modules.stream()
                .filter(m -> m.getClass() == clazz)
                .findFirst().orElse(null);
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(ModuleCategory category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }
}
