package baritone.pathing;

import baritone.Baritone;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class FollowProcess {
    private final Baritone baritone;
    private Entity target;
    private boolean active;

    public FollowProcess(Baritone baritone) {
        this.baritone = baritone;
    }

    public void follow(Entity entity) {
        this.target = entity;
        this.active = true;
    }

    public void followPlayer(String name) {
        if (baritone.getMinecraft().world == null) return;
        for (PlayerEntity player : baritone.getMinecraft().world.getPlayers()) {
            if (player.getName().getString().equalsIgnoreCase(name)) {
                follow(player);
                return;
            }
        }
    }

    public void cancel() {
        this.target = null;
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public Entity getTarget() {
        return target;
    }

    public void tick() {
        if (!active || target == null) return;
        if (baritone.getMinecraft().player == null) return;

        double radius = baritone.getSettings().followRadius;
        if (baritone.getMinecraft().player.getDistance(target) <= radius) {
            return;
        }

        BlockPos targetPos = target.getPosition();
        baritone.getPathingBehavior().setGoal(targetPos);
        baritone.getPathingBehavior().path();
    }
}
