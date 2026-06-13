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

public class JumpTrigger implements Listener {

    private final NovaRelics plugin;
    private final Map<UUID, Boolean> wasOnGround = new ConcurrentHashMap<>();

    public JumpTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        boolean onGround = player.isOnGround();
        Boolean prevOnGround = wasOnGround.get(uuid);

        if (prevOnGround != null && prevOnGround && !onGround &&
                event.getTo() != null &&
                event.getTo().getY() > event.getFrom().getY()) {
            plugin.getTriggerManager().dispatchFromAllSlots(player, TriggerType.JUMP, event);
        }

        wasOnGround.put(uuid, onGround);
    }
}
