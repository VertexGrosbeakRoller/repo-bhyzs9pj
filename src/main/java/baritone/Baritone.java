package baritone;

import baritone.api.BaritoneAPI;
import baritone.api.IBaritone;
import baritone.api.Settings;
import baritone.pathing.PathingBehavior;
import baritone.pathing.MineProcess;
import baritone.pathing.FollowProcess;
import baritone.pathing.GoToProcess;
import net.minecraft.client.Minecraft;

/**
 * Baritone — интегрированный pathfinding бот для Minecraft.
 * Добавлен как исходный код (source), НЕ как отдельный мод.
 * Основные процессы: Mine, Follow, GoTo, Path.
 */
public class Baritone implements IBaritone {
    private static Baritone instance;
    private final Minecraft mc = Minecraft.getInstance();
    private final Settings settings = new Settings();
    private final PathingBehavior pathingBehavior;
    private final MineProcess mineProcess;
    private final FollowProcess followProcess;
    private final GoToProcess goToProcess;

    public Baritone() {
        this.pathingBehavior = new PathingBehavior(this);
        this.mineProcess = new MineProcess(this);
        this.followProcess = new FollowProcess(this);
        this.goToProcess = new GoToProcess(this);
    }

    public static Baritone getInstance() {
        if (instance == null) {
            instance = new Baritone();
            BaritoneAPI.setInstance(instance);
        }
        return instance;
    }

    @Override
    public Settings getSettings() { return settings; }

    @Override
    public PathingBehavior getPathingBehavior() { return pathingBehavior; }

    @Override
    public MineProcess getMineProcess() { return mineProcess; }

    @Override
    public FollowProcess getFollowProcess() { return followProcess; }

    @Override
    public GoToProcess getGoToProcess() { return goToProcess; }

    public Minecraft getMinecraft() { return mc; }
}
