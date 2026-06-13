package dev.aponder.novarelics.cooldown;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private static final String GLOBAL_UUID = "00000000-0000-0000-0000-000000000000";

    private final NovaRelics plugin;
    // Memory cache: uuid -> (cooldownKey -> expiresAt)
    private final Map<String, Map<String, Long>> memoryCache = new ConcurrentHashMap<>();
    private final boolean useDatabase;

    public CooldownManager(NovaRelics plugin) {
        this.plugin = plugin;
        this.useDatabase = "DATABASE".equalsIgnoreCase(
                plugin.getConfigManager().getMainConfig()
                        .getString("cooldown.storage", "MEMORY"));
    }

    public boolean isOnCooldown(Player player, RelicDefinition relic) {
        String key = buildKey(player.getUniqueId(), relic);
        return getRemainingMs(key, resolveUuid(player, relic)) > 0;
    }

    public long getRemainingSeconds(Player player, RelicDefinition relic) {
        String key = buildKey(player.getUniqueId(), relic);
        return getRemainingMs(key, resolveUuid(player, relic)) / 1000L;
    }

    public void applyCooldown(Player player, RelicDefinition relic) {
        long durationSec = relic.getCooldownSeconds();
        if (durationSec <= 0) return;

        String key = buildKey(player.getUniqueId(), relic);
        UUID uuid = resolveUuid(player, relic);
        long expiresAt = System.currentTimeMillis() + (durationSec * 1000L);

        if (useDatabase) {
            plugin.getStorageManager().setCooldown(uuid, key, durationSec * 1000L);
        } else {
            memoryCache
                .computeIfAbsent(uuid.toString(), k -> new ConcurrentHashMap<>())
                .put(key, expiresAt);
        }
    }

    public void clearCooldown(Player player, RelicDefinition relic) {
        String key = buildKey(player.getUniqueId(), relic);
        UUID uuid = resolveUuid(player, relic);
        if (useDatabase) {
            plugin.getStorageManager().clearCooldown(uuid, key);
        } else {
            Map<String, Long> map = memoryCache.get(uuid.toString());
            if (map != null) map.remove(key);
        }
    }

    public void notifyPlayer(Player player, RelicDefinition relic) {
        if (!plugin.getConfigManager().getMainConfig()
                .getBoolean("cooldown.notify-on-cooldown", true)) return;
        long remaining = getRemainingSeconds(player, relic);
        String msg = plugin.getConfigManager().getMessage("cooldown-active",
                "<time>", String.valueOf(remaining));
        MiniMsg.send(player, MiniMsg.parse(msg));
    }

    private long getRemainingMs(String key, UUID uuid) {
        if (useDatabase) {
            return plugin.getStorageManager().getRemainingCooldown(uuid, key);
        }
        Map<String, Long> map = memoryCache.get(uuid.toString());
        if (map == null) return 0;
        Long expires = map.get(key);
        if (expires == null) return 0;
        long remaining = expires - System.currentTimeMillis();
        if (remaining <= 0) {
            map.remove(key);
            return 0;
        }
        return remaining;
    }

    private String buildKey(UUID playerUuid, RelicDefinition relic) {
        String sharedKey = relic.getCooldownSharedKey();
        if (sharedKey != null && !sharedKey.isEmpty()) {
            return "shared:" + sharedKey;
        }
        return "relic:" + relic.getId();
    }

    private UUID resolveUuid(Player player, RelicDefinition relic) {
        if (relic.getCooldownType() == CooldownType.GLOBAL) {
            return UUID.fromString(GLOBAL_UUID);
        }
        return player.getUniqueId();
    }

    public void clearPlayer(UUID uuid) {
        memoryCache.remove(uuid.toString());
    }
}
