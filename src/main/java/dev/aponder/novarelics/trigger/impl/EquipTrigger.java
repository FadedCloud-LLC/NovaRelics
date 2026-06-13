package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class EquipTrigger implements Listener {

    private final NovaRelics plugin;

    public EquipTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack prev = player.getInventory().getItem(event.getPreviousSlot());
        ItemStack next = player.getInventory().getItem(event.getNewSlot());

        // UNEQUIP previous
        if (prev != null && plugin.getRelicManager().isRelic(prev)) {
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(prev);
            if (relic != null && relic.hasTrigger(TriggerType.UNEQUIP)) {
                plugin.getTriggerManager().dispatch(player, prev, relic,
                        TriggerType.UNEQUIP, event);
            }
        }

        // EQUIP next
        if (next != null && plugin.getRelicManager().isRelic(next)) {
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(next);
            if (relic != null && relic.hasTrigger(TriggerType.EQUIP)) {
                plugin.getTriggerManager().dispatch(player, next, relic,
                        TriggerType.EQUIP, event);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onArmorChange(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getInventory().getType() != InventoryType.PLAYER) return;

        // Armor slots are 36-39 in the player inventory
        int slot = event.getRawSlot();
        if (slot < 5 || slot > 8) return; // Armor slots in GUI

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && plugin.getRelicManager().isRelic(cursor)) {
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(cursor);
            if (relic != null)
                plugin.getTriggerManager().dispatch(player, cursor, relic, TriggerType.EQUIP, event);
        }
        if (current != null && plugin.getRelicManager().isRelic(current)) {
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(current);
            if (relic != null)
                plugin.getTriggerManager().dispatch(player, current, relic, TriggerType.UNEQUIP, event);
        }
    }
}
