package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class WorldChangeTrigger implements Listener {

    private final NovaRelics plugin;

    public WorldChangeTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getPlayer(), TriggerType.WORLD_CHANGE, event);
    }
}
