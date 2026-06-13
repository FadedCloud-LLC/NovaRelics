package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakTrigger implements Listener {

    private final NovaRelics plugin;

    public SneakTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return; // Only fire when starting to sneak
        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getPlayer(), TriggerType.SNEAK, event);
    }
}
