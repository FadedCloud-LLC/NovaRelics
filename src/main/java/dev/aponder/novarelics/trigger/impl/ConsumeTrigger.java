package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ConsumeTrigger implements Listener {

    private final NovaRelics plugin;

    public ConsumeTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (!plugin.getRelicManager().isRelic(item)) return;

        RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(item);
        if (relic == null || !relic.hasTrigger(TriggerType.CONSUME)) return;

        plugin.getTriggerManager().dispatch(player, item, relic, TriggerType.CONSUME, event);
    }
}
