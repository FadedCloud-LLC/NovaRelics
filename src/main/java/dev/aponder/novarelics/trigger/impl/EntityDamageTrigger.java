package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageTrigger implements Listener {

    private final NovaRelics plugin;

    public EntityDamageTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    // Player DEALS damage
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamageByPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        plugin.getTriggerManager().dispatchFromHand(player, TriggerType.ENTITY_DAMAGE, event);
    }
}
