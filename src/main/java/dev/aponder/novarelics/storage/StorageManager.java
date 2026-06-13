package dev.aponder.novarelics.storage;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.storage.model.PlayerData;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StorageManager {

    private final NovaRelics plugin;
    private StorageProvider provider;
    private final Map<UUID, PlayerData> playerCache = new ConcurrentHashMap<>();

    public StorageManager(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        Configurator.setLevel("dev.aponder.novarelics.libs.hikari", org.apache.logging.log4j.Level.WARN);

        String type = plugin.getConfigManager().getMainConfig()
                .getString("storage.type", "SQLITE").toUpperCase();
        provider = type.equals("MYSQL") ? new MySQLProvider(plugin) : new SQLiteProvider(plugin);
        try {
            provider.connect();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to storage! Falling back to SQLite.", e);
            provider = new SQLiteProvider(plugin);
            try {
                provider.connect();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "SQLite fallback also failed!", ex);
            }
        }
    }

    public void disconnect() {
        // Flush all cached player data
        playerCache.values().forEach(data -> {
            data.setLastSeen(System.currentTimeMillis());
            provider.savePlayer(data);
        });
        playerCache.clear();
        if (provider != null) provider.disconnect();
    }

    public PlayerData getPlayer(UUID uuid) {
        return playerCache.computeIfAbsent(uuid, id -> provider.loadPlayer(id));
    }

    public void savePlayer(UUID uuid) {
        PlayerData data = playerCache.get(uuid);
        if (data != null) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    () -> provider.savePlayer(data));
        }
    }

    public void unloadPlayer(UUID uuid) {
        PlayerData data = playerCache.remove(uuid);
        if (data != null) {
            data.setLastSeen(System.currentTimeMillis());
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    () -> provider.savePlayer(data));
        }
    }

    public boolean isOnCooldown(UUID uuid, String key) {
        return getRemainingCooldown(uuid, key) > 0;
    }

    public long getRemainingCooldown(UUID uuid, String key) {
        long now = System.currentTimeMillis();
        long expires = provider.getCooldownExpiry(uuid, key);
        return Math.max(0, expires - now);
    }

    public void setCooldown(UUID uuid, String key, long durationMillis) {
        long expiresAt = System.currentTimeMillis() + durationMillis;
        provider.setCooldown(uuid, key, expiresAt);
    }

    public void clearCooldown(UUID uuid, String key) {
        provider.removeCooldown(uuid, key);
    }

    public int getCharges(UUID uuid, String itemUuid, String relicId) {
        return provider.getCharges(uuid, itemUuid, relicId);
    }

    public void setCharges(UUID uuid, String itemUuid, String relicId, int charges) {
        provider.setCharges(uuid, itemUuid, relicId, charges);
    }

    public StorageProvider getProvider() { return provider; }
}
