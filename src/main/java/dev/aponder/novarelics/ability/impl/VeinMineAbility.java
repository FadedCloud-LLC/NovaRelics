package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VeinMineAbility extends Ability {

    private static final int[][] NEIGHBORS = {
        {1,0,0},{-1,0,0},{0,1,0},{0,-1,0},{0,0,1},{0,0,-1}
    };

    public VeinMineAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        BlockBreakEvent event = context.getEvent(BlockBreakEvent.class);
        if (event == null) return false;

        Player player = context.getPlayer();
        Block origin = event.getBlock();
        Material targetMaterial = origin.getType();
        int maxBlocks = config.getInt("max-blocks",
                plugin.getConfigManager().getMainConfig()
                        .getInt("performance.max-vein-mine-blocks", 64));

        List<Block> vein = findVein(origin, targetMaterial, maxBlocks);
        if (vein.size() <= 1) return false;

        ItemStack tool = player.getInventory().getItemInMainHand();
        int broken = 0;
        for (Block block : vein) {
            if (block.equals(origin)) continue;
            block.getDrops(tool).forEach(drop ->
                    block.getWorld().dropItemNaturally(block.getLocation(), drop));
            block.setType(Material.AIR);
            broken++;
        }

        if (broken > 0) {
            String msg = plugin.getConfigManager().getMessage("ability-vein-mine",
                    "<count>", String.valueOf(broken + 1));
            MiniMsg.send(player, MiniMsg.parse(msg));
        }
        return true;
    }

    private List<Block> findVein(Block origin, Material material, int maxBlocks) {
        List<Block> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<Block> queue = new ArrayDeque<>();
        queue.add(origin);
        visited.add(key(origin));

        while (!queue.isEmpty() && result.size() < maxBlocks) {
            Block current = queue.poll();
            result.add(current);
            for (int[] n : NEIGHBORS) {
                Block neighbor = current.getRelative(n[0], n[1], n[2]);
                String nKey = key(neighbor);
                if (!visited.contains(nKey) && neighbor.getType() == material) {
                    visited.add(nKey);
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }

    private String key(Block b) {
        return b.getX() + "," + b.getY() + "," + b.getZ();
    }

    @Override
    public String getName() { return "Vein Mine"; }

    @Override
    public String getDescription() { return "Mines all connected blocks of the same type."; }
}
