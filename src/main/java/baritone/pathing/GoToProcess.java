package baritone.pathing;

import baritone.Baritone;
import net.minecraft.util.math.BlockPos;

public class GoToProcess {
    private final Baritone baritone;
    private BlockPos destination;
    private boolean active;

    public GoToProcess(Baritone baritone) {
        this.baritone = baritone;
    }

    public void goTo(BlockPos pos) {
        this.destination = pos;
        this.active = true;
        baritone.getPathingBehavior().setGoal(pos);
        baritone.getPathingBehavior().path();
    }

    public void goTo(int x, int y, int z) {
        goTo(new BlockPos(x, y, z));
    }

    public void cancel() {
        this.destination = null;
        this.active = false;
        baritone.getPathingBehavior().cancel();
    }

    public boolean isActive() {
        return active;
    }

    public BlockPos getDestination() {
        return destination;
    }
}
