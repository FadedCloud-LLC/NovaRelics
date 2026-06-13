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
import org.bukkit.inventory.SmithingInventory;

public class SmithingTableTrigger implements Listener {

    private final NovaRelics plugin;

    public SmithingTableTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getType() != InventoryType.SMITHING) return;
        if (event.getRawSlot() != 3) return;

        SmithingInventory inv = (SmithingInventory) event.getInventory();
        // Check all input slots for a relic
        for (int i = 0; i < 3; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || !plugin.getRelicManager().isRelic(item)) continue;
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(item);
            if (relic == null || !relic.hasTrigger(TriggerType.SMITHING_TABLE)) continue;
            plugin.getTriggerManager().dispatch(player, item, relic,
                    TriggerType.SMITHING_TABLE, event);
        }
    }
}
