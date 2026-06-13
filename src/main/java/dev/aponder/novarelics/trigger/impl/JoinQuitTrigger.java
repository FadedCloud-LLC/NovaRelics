package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuitTrigger implements Listener {

    private final NovaRelics plugin;

    public JoinQuitTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getStorageManager().getPlayer(event.getPlayer().getUniqueId());
        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getPlayer(), TriggerType.JOIN, event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTriggerManager().dispatchFromAllSlots(
                event.getPlayer(), TriggerType.QUIT, event);
        plugin.getStorageManager().unloadPlayer(event.getPlayer().getUniqueId());
        plugin.getCooldownManager().clearPlayer(event.getPlayer().getUniqueId());
    }
}
