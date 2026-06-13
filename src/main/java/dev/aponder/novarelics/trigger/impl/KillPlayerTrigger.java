package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillPlayerTrigger implements Listener {

    private final NovaRelics plugin;

    public KillPlayerTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        plugin.getTriggerManager().dispatchFromAllSlots(killer, TriggerType.KILL_PLAYER, event);
    }
}
