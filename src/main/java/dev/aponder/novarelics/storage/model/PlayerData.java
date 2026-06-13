package dev.aponder.novarelics.storage.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final Set<String> discoveredRelics;
    private long lastSeen;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.discoveredRelics = new HashSet<>();
        this.lastSeen = System.currentTimeMillis();
    }

    public UUID getUuid() { return uuid; }

    public Set<String> getDiscoveredRelics() { return discoveredRelics; }

    public void addDiscovered(String relicId) { discoveredRelics.add(relicId); }

    public boolean hasDiscovered(String relicId) { return discoveredRelics.contains(relicId); }

    public long getLastSeen() { return lastSeen; }

    public void setLastSeen(long lastSeen) { this.lastSeen = lastSeen; }

    public int getDiscoveredCount() { return discoveredRelics.size(); }
}
