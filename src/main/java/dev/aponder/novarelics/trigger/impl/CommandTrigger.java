package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CommandTrigger implements Listener {

    private final NovaRelics plugin;

    public CommandTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        for (ItemStack item : getAllRelicItems(player)) {
            RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(item);
            if (relic == null || !relic.hasTrigger(TriggerType.COMMAND)) continue;

            // Check if any ability config has a matching command trigger pattern
            relic.getAbilitiesForTrigger(TriggerType.COMMAND).forEach(cfg -> {
                String pattern = cfg.getString("command-pattern", "");
                if (!pattern.isEmpty() && message.startsWith("/" + pattern.toLowerCase())) {
                    plugin.getTriggerManager().dispatch(player, item, relic,
                            TriggerType.COMMAND, event);
                }
            });
        }
    }

    private List<ItemStack> getAllRelicItems(Player player) {
        List<ItemStack> items = new java.util.ArrayList<>();
        ItemStack main = player.getInventory().getItemInMainHand();
        if (plugin.getRelicManager().isRelic(main)) items.add(main);
        ItemStack off = player.getInventory().getItemInOffHand();
        if (plugin.getRelicManager().isRelic(off)) items.add(off);
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor != null && plugin.getRelicManager().isRelic(armor)) items.add(armor);
        }
        return items;
    }
}
