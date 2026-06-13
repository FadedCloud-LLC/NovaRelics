package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class GrindstoneTrigger implements Listener {

    private final NovaRelics plugin;

    public GrindstoneTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getType() != InventoryType.GRINDSTONE) return;
        if (event.getRawSlot() != 2) return; // Result slot

        for (int i = 0; i < 2; i++) {
            ItemStack item = event.getInventory().getItem(i);
            if (item == null || !plugin.getRelicManager().isRelic(item)) continue;
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(item);
            if (relic == null || !relic.hasTrigger(TriggerType.GRINDSTONE)) continue;
            plugin.getTriggerManager().dispatch(player, item, relic,
                    TriggerType.GRINDSTONE, event);
        }
    }
}
