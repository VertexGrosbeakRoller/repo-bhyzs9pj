package baritone.command;

import baritone.Baritone;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CommandManager {
    private final Baritone baritone;
    private final Map<String, Consumer<String[]>> commands = new HashMap<>();

    public CommandManager(Baritone baritone) {
        this.baritone = baritone;
        registerDefaults();
    }

    private void registerDefaults() {
        commands.put("goto", args -> {
            if (args.length >= 3) {
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                int z = Integer.parseInt(args[2]);
                baritone.getGoToProcess().goTo(x, y, z);
            }
        });

        commands.put("mine", args -> {
            if (args.length >= 1) {
                String blockName = args[0].toLowerCase();
                switch (blockName) {
                    case "diamond_ore":
                        baritone.getMineProcess().mine(Blocks.DIAMOND_ORE);
                        break;
                    case "iron_ore":
                        baritone.getMineProcess().mine(Blocks.IRON_ORE);
                        break;
                    case "gold_ore":
                        baritone.getMineProcess().mine(Blocks.GOLD_ORE);
                        break;
                    case "ancient_debris":
                        baritone.getMineProcess().mine(Blocks.ANCIENT_DEBRIS);
                        break;
                    default:
                        break;
                }
            }
        });

        commands.put("follow", args -> {
            if (args.length >= 1) {
                baritone.getFollowProcess().followPlayer(args[0]);
            }
        });

        commands.put("cancel", args -> {
            baritone.getPathingBehavior().cancel();
            baritone.getMineProcess().cancel();
            baritone.getFollowProcess().cancel();
            baritone.getGoToProcess().cancel();
        });

        commands.put("stop", args -> {
            commands.get("cancel").accept(args);
        });
    }

    public boolean execute(String command) {
        String prefix = baritone.getSettings().chatPrefix;
        if (!command.startsWith(prefix)) return false;

        String[] parts = command.substring(prefix.length()).trim().split("\\s+");
        if (parts.length == 0) return false;

        String cmd = parts[0].toLowerCase();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Consumer<String[]> handler = commands.get(cmd);
        if (handler != null) {
            handler.accept(args);
            return true;
        }
        return false;
    }
}
