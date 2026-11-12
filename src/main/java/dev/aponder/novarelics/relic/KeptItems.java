package dev.aponder.novarelics.relic;

import dev.aponder.novarelics.NovaRelics;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Items a player keeps through death (KEEP_ITEM, NR-007).
 *
 * <p>The server clears a dead player's inventory after PlayerDeathEvent unless keepInventory
 * is on, so the item cannot simply be put back during the event. Instead it is removed from
 * the drops, stored in the player's PersistentDataContainer (saved with the player file, so it
 * survives disconnects and restarts) and returned on respawn, or on join if the player left
 * while dead. Works identically on Spigot and Paper.
 */
public class KeptItems implements Listener {

    private final NovaRelics plugin;
    private final NamespacedKey key;

    public KeptItems(NovaRelics plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "kept_items");
    }

    /** Stores an item to be returned when the player is next alive. */
    public void keep(Player player, ItemStack item) {
        List<ItemStack> items = read(player.getPersistentDataContainer());
        items.add(item.clone());
        write(player.getPersistentDataContainer(), items);
    }

    /** Gives back every stored item (overflow is dropped at the player's feet). */
    public int restore(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(key, PersistentDataType.BYTE_ARRAY)) return 0;
        List<ItemStack> items = read(pdc);
        pdc.remove(key);
        for (ItemStack item : items) {
            player.getInventory().addItem(item).values()
                    .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
        }
        return items.size();
    }

    public boolean hasPending(Player player) {
        return player.getPersistentDataContainer().has(key, PersistentDataType.BYTE_ARRAY);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        restore(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!p.isDead() && hasPending(p)) restore(p);
    }

    private List<ItemStack> read(PersistentDataContainer pdc) {
        byte[] bytes = pdc.get(key, PersistentDataType.BYTE_ARRAY);
        List<ItemStack> items = new ArrayList<>();
        if (bytes == null) return items;
        try (BukkitObjectInputStream in = new BukkitObjectInputStream(new ByteArrayInputStream(bytes))) {
            int n = in.readInt();
            for (int i = 0; i < n; i++) items.add((ItemStack) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read kept relics; data left untouched", e);
            throw new IllegalStateException(e);
        }
        return items;
    }

    private void write(PersistentDataContainer pdc, List<ItemStack> items) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(bytes)) {
            out.writeInt(items.size());
            for (ItemStack item : items) out.writeObject(item);
            out.flush();
            pdc.set(key, PersistentDataType.BYTE_ARRAY, bytes.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Could not store kept relic", e);
        }
    }
}
