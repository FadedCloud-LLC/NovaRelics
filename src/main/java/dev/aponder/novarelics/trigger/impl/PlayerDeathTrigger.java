package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathTrigger implements Listener {

    private final NovaRelics plugin;

    public PlayerDeathTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getEntity(), TriggerType.PLAYER_DEATH, event);
    }
}
