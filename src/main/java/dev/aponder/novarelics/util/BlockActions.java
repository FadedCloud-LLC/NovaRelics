package dev.aponder.novarelics.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * World changes made by abilities on a player's behalf, done so that protection plugins
 * (WorldGuard, GriefPrevention, Lands, Towny...) and loggers (CoreProtect) see them (NR-004).
 *
 * <p>Breaks go through {@link Player#breakBlock(Block)}, which fires a real BlockBreakEvent,
 * applies tool durability and uses vanilla drops. While an ability is breaking blocks, the
 * player is marked as "cascading" so those secondary breaks do not trigger relics again.
 */
public final class BlockActions {

    private static final Set<UUID> CASCADING = Collections.synchronizedSet(new HashSet<>());

    private BlockActions() { }

    /** True while {@code player}'s blocks are being broken by an ability (not by the player). */
    public static boolean isCascading(Player player) {
        return CASCADING.contains(player.getUniqueId());
    }

    /**
     * Breaks each block as the player, stopping if the held tool breaks.
     *
     * @return how many blocks were actually broken (protection may deny some)
     */
    public static int breakAsPlayer(Player player, Iterable<Block> blocks) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        Material toolType = tool.getType();
        int broken = 0;
        if (!CASCADING.add(player.getUniqueId())) return 0; // already cascading: never recurse
        try {
            for (Block block : blocks) {
                if (!toolType.isAir() && player.getInventory().getItemInMainHand().getType() != toolType) {
                    break; // tool broke or was swapped mid-cascade
                }
                if (block.getType().isAir()) continue;
                if (player.breakBlock(block)) broken++;
            }
        } finally {
            CASCADING.remove(player.getUniqueId());
        }
        return broken;
    }

    /**
     * Places {@code material} at {@code block} only if a BlockPlaceEvent fired for the player
     * is not cancelled (i.e. the player could legitimately build there).
     */
    public static boolean placeAsPlayer(Player player, Block block, Material material) {
        BlockState before = block.getState();
        BlockPlaceEvent event = new BlockPlaceEvent(block, before, block.getRelative(BlockFace.DOWN),
                new ItemStack(material), player, true, EquipmentSlot.HAND);
        boolean marked = CASCADING.add(player.getUniqueId());
        try {
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
        } finally {
            if (marked) CASCADING.remove(player.getUniqueId());
        }
        if (event.isCancelled() || !event.canBuild()) return false;
        block.setType(material);
        return true;
    }
}
