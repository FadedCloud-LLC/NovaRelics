package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TreeFellerAbility extends Ability {

    private static final int[][] NEIGHBORS_26 = buildNeighbors();

    public TreeFellerAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        BlockBreakEvent event = context.getEvent(BlockBreakEvent.class);
        if (event == null) return false;

        Block origin = event.getBlock();
        if (!Tag.LOGS.isTagged(origin.getType())) return false;

        Player player = context.getPlayer();
        int maxBlocks = config.getInt("max-blocks",
                plugin.getConfigManager().getMainConfig()
                        .getInt("performance.max-tree-feller-blocks", 128));

        List<Block> tree = findTree(origin, maxBlocks);
        ItemStack tool = player.getInventory().getItemInMainHand();

        int felled = 0;
        for (Block block : tree) {
            if (block.equals(origin)) continue;
            block.getDrops(tool).forEach(drop ->
                    block.getWorld().dropItemNaturally(block.getLocation(), drop));
            block.setType(Material.AIR);
            felled++;
        }

        if (felled > 0) {
            String msg = plugin.getConfigManager().getMessage("ability-tree-feller",
                    "<count>", String.valueOf(felled + 1));
            MiniMsg.send(player, MiniMsg.parse(msg));
        }
        return true;
    }

    private List<Block> findTree(Block origin, int max) {
        List<Block> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(key(origin));

        while (!queue.isEmpty() && result.size() < max) {
            Block current = queue.poll();
            result.add(current);
            for (int[] n : NEIGHBORS_26) {
                Block nb = current.getRelative(n[0], n[1], n[2]);
                String k = key(nb);
                if (!visited.contains(k) && Tag.LOGS.isTagged(nb.getType())) {
                    visited.add(k);
                    queue.add(nb);
                }
            }
        }
        return result;
    }

    private static int[][] buildNeighbors() {
        List<int[]> neighbors = new ArrayList<>();
        for (int x = -1; x <= 1; x++)
            for (int y = -1; y <= 1; y++)
                for (int z = -1; z <= 1; z++)
                    if (x != 0 || y != 0 || z != 0)
                        neighbors.add(new int[]{x, y, z});
        return neighbors.toArray(new int[0][]);
    }

    private String key(Block b) {
        return b.getX() + "," + b.getY() + "," + b.getZ();
    }

    @Override
    public String getName() { return "Tree Feller"; }

    @Override
    public String getDescription() { return "Fells an entire tree when a log is broken."; }
}
