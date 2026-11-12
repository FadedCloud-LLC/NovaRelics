package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceBlockTrigger implements Listener {

    private final NovaRelics plugin;

    public PlaceBlockTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        // Placements made by an ability (e.g. web trap) must not re-trigger relics.
        if (dev.aponder.novarelics.util.BlockActions.isCascading(event.getPlayer())) return;
        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getPlayer(), TriggerType.PLACE_BLOCK, event);
    }
}
