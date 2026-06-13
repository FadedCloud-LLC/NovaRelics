package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamagedTrigger implements Listener {

    private final NovaRelics plugin;

    public EntityDamagedTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    // Player RECEIVES damage
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        plugin.getTriggerManager().dispatchFromAllSlots(player, TriggerType.ENTITY_DAMAGED, event);
    }
}
