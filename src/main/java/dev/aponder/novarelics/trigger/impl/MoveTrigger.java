package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MoveTrigger implements Listener {

    private final NovaRelics plugin;
    private final Map<UUID, Long> lastFire = new ConcurrentHashMap<>();
    private static final long THROTTLE_MS = 1000L; // 1 second

    public MoveTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) return;

        Player player = event.getPlayer();
        long now = System.currentTimeMillis();
        Long last = lastFire.get(player.getUniqueId());
        if (last != null && now - last < THROTTLE_MS) return;
        lastFire.put(player.getUniqueId(), now);

        plugin.getTriggerManager().dispatchFromAllSlots(player, TriggerType.MOVE, event);
    }
}
