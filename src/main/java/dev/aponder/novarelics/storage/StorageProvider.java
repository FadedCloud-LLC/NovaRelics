package dev.aponder.novarelics.storage;

import dev.aponder.novarelics.storage.model.PlayerData;

import java.util.UUID;

public interface StorageProvider {

    void connect() throws Exception;

    void disconnect();

    void createTables();

    PlayerData loadPlayer(UUID uuid);

    void savePlayer(PlayerData data);

    long getCooldownExpiry(UUID uuid, String cooldownKey);

    void setCooldown(UUID uuid, String cooldownKey, long expiresAt);

    void removeCooldown(UUID uuid, String cooldownKey);

    int getCharges(UUID uuid, String itemUuid, String relicId);

    void setCharges(UUID uuid, String itemUuid, String relicId, int charges);
}
