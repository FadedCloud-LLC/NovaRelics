package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilTrigger implements Listener {

    private final NovaRelics plugin;

    public AnvilTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    // Phase 1 — prepare the anvil result
    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepare(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack second = inv.getItem(1);
        if (second == null || !plugin.getRelicManager().isRelic(second)) return;

        RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(second);
        if (relic == null || !relic.hasTrigger(TriggerType.ANVIL)) return;

        HumanEntity viewer = event.getViewers().isEmpty() ? null : event.getViewers().get(0);
        if (!(viewer instanceof Player player)) return;

        plugin.getTriggerManager().dispatchPrepare(player, second, relic,
                TriggerType.ANVIL, event);
    }

    // Phase 2 — player takes the result, fire execute
    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getType() != InventoryType.ANVIL) return;
        if (event.getRawSlot() != 2) return; // Result slot

        AnvilInventory inv = (AnvilInventory) event.getInventory();
        ItemStack result = inv.getItem(2);
        ItemStack second = inv.getItem(1);

        if (result == null || result.getType().isAir()) return;
        if (second == null || !plugin.getRelicManager().isRelic(second)) return;

        RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(second);
        if (relic == null || !relic.hasTrigger(TriggerType.ANVIL)) return;

        plugin.getTriggerManager().dispatch(player, second, relic,
                TriggerType.ANVIL, event);
    }
}
