package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BreakBlockTrigger implements Listener {

    private final NovaRelics plugin;

    public BreakBlockTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // Blocks broken by an ability (vein mine, tree feller) must not re-trigger relics.
        if (dev.aponder.novarelics.util.BlockActions.isCascading(player)) return;

        // Check off-hand too for passive relics
        plugin.getTriggerManager().dispatchFromAllSlots(player, TriggerType.BREAK_BLOCK, event);
    }
}
