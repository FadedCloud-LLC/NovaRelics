package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public class FishTrigger implements Listener {

    private final NovaRelics plugin;

    public FishTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH &&
            event.getState() != PlayerFishEvent.State.CAUGHT_ENTITY) return;

        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getPlayer(), TriggerType.FISH, event);
    }
}
