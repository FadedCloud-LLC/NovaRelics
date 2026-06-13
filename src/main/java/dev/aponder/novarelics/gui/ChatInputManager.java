package dev.aponder.novarelics.gui;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputManager implements Listener {

    private final NovaRelics plugin;
    private final Map<UUID, Consumer<String>> pending = new ConcurrentHashMap<>();

    public ChatInputManager(NovaRelics plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void request(Player player, String prompt, Consumer<String> callback) {
        player.closeInventory();
        MiniMsg.send(player, MiniMsg.parse(prompt));
        MiniMsg.send(player, MiniMsg.parse("<dark_gray>Type <red>cancel</red> to abort."));
        pending.put(player.getUniqueId(), callback);
    }

    public void cancel(Player player) {
        pending.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Consumer<String> callback = pending.remove(event.getPlayer().getUniqueId());
        if (callback == null) return;
        event.setCancelled(true);
        String input = event.getMessage();
        if (!input.equalsIgnoreCase("cancel")) {
            plugin.getServer().getScheduler().runTask(plugin, () -> callback.accept(input));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pending.remove(event.getPlayer().getUniqueId());
    }
}
