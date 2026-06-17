package baritone.pathing;

import baritone.Baritone;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class MineProcess {
    private final Baritone baritone;
    private Block targetBlock;
    private boolean active;
    private final List<BlockPos> knownLocations = new ArrayList<>();

    public MineProcess(Baritone baritone) {
        this.baritone = baritone;
    }

    public void mine(Block block) {
        this.targetBlock = block;
        this.active = true;
        scanForBlocks();
    }

    public void mine(int count, Block... blocks) {
        if (blocks.length > 0) {
            mine(blocks[0]);
        }
    }

    public void cancel() {
        this.targetBlock = null;
        this.active = false;
        knownLocations.clear();
    }

    public boolean isActive() {
        return active;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    private void scanForBlocks() {
        if (baritone.getMinecraft().player == null || baritone.getMinecraft().world == null) return;
        knownLocations.clear();

        BlockPos center = baritone.getMinecraft().player.getPosition();
        int radius = 32;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.add(x, y, z);
                    if (baritone.getMinecraft().world.getBlockState(pos).getBlock() == targetBlock) {
                        knownLocations.add(pos);
                    }
                }
            }
        }
    }

    public List<BlockPos> getKnownLocations() {
        return knownLocations;
    }
}
