package baritone.pathing;

import baritone.Baritone;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PathingBehavior {
    private final Baritone baritone;
    private boolean isPathing;
    private BlockPos goal;
    private List<BlockPos> currentPath;

    public PathingBehavior(Baritone baritone) {
        this.baritone = baritone;
    }

    public boolean isPathing() {
        return isPathing;
    }

    public void setGoal(BlockPos goal) {
        this.goal = goal;
    }

    public BlockPos getGoal() {
        return goal;
    }

    public void path() {
        if (goal == null) return;
        isPathing = true;
    }

    public void cancel() {
        isPathing = false;
        goal = null;
        currentPath = null;
    }

    public List<BlockPos> getCurrentPath() {
        return currentPath;
    }

    public void tick() {
        if (!isPathing || goal == null) return;
        if (baritone.getMinecraft().player == null) return;

        BlockPos playerPos = baritone.getMinecraft().player.getPosition();
        if (playerPos.distanceSq(goal) < 4) {
            cancel();
        }
    }
}
