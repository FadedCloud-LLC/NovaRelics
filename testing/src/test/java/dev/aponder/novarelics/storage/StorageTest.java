package dev.aponder.novarelics.storage;

import dev.aponder.novarelics.storage.model.PlayerData;
import dev.aponder.novarelics.support.PluginTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class StorageTest extends PluginTestBase {

    private StorageManager storage() { return plugin.getStorageManager(); }

    @Test
    void usesSqliteByDefault() {
        assertEquals("SQLite", storage().getProviderName());
        assertInstanceOf(SQLiteProvider.class, storage().getProvider());
    }

    @Test
    void playerDataPersistsAcrossUnload() {
        UUID id = UUID.randomUUID();
        storage().getPlayer(id).addDiscovered("ore_charm");
        storage().unloadPlayer(id);
        server.getScheduler().waitAsyncTasksFinished();
        PlayerData reloaded = storage().getPlayer(id);
        assertTrue(reloaded.hasDiscovered("ore_charm"));
        assertTrue(reloaded.getLastSeen() > 0);
    }

    @Test
    void playerDataPersistsAcrossRestart() {
        UUID id = UUID.randomUUID();
        storage().getPlayer(id).addDiscovered("teleport_crystal");
        server.getPluginManager().disablePlugin(plugin);
        server.getPluginManager().enablePlugin(plugin);
        assertTrue(plugin.getStorageManager().getPlayer(id).hasDiscovered("teleport_crystal"));
    }

    @Test
    void cooldownRowsUpsertAndClear() {
        UUID id = UUID.randomUUID();
        storage().setCooldown(id, "relic:x", 60_000);
        long r1 = storage().getRemainingCooldown(id, "relic:x");
        assertTrue(r1 > 55_000 && r1 <= 60_000);
        storage().setCooldown(id, "relic:x", 5_000);
        assertTrue(storage().getRemainingCooldown(id, "relic:x") <= 5_000, "upsert replaced the row");
        storage().clearCooldown(id, "relic:x");
        assertFalse(storage().isOnCooldown(id, "relic:x"));
        assertEquals(0, storage().getRemainingCooldown(UUID.randomUUID(), "missing"));
    }

    @Test
    void chargesRoundTrip() {
        UUID id = UUID.randomUUID();
        assertEquals(-1, storage().getCharges(id, "item-1", "r"));
        storage().setCharges(id, "item-1", "r", 7);
        assertEquals(7, storage().getCharges(id, "item-1", "r"));
    }

    @Test
    void hostileStringsAreStoredLiterally() {
        UUID id = UUID.randomUUID();
        String key = "relic:'; DROP TABLE nr_cooldowns; --";
        storage().setCooldown(id, key, 10_000);
        assertTrue(storage().isOnCooldown(id, key));
        storage().setCooldown(id, "relic:other", 10_000);
        assertTrue(storage().isOnCooldown(id, "relic:other"), "table still exists");
    }

    @Test
    void concurrentWritesFromManyThreads() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            futures.add(pool.submit(() -> storage().setCooldown(id, "relic:c", 60_000)));
        }
        for (Future<?> f : futures) f.get(30, TimeUnit.SECONDS);
        pool.shutdown();
        for (UUID id : ids) assertTrue(storage().isOnCooldown(id, "relic:c"));
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-024 storage that cannot start disables the plugin instead of failing later with NPEs")
    void brokenStorageDisablesPlugin() throws Exception {
        server.getPluginManager().disablePlugin(plugin);
        // Make data.db a directory so neither SQLite (nor the MySQL fallback) can open it
        java.io.File db = new java.io.File(plugin.getDataFolder(), "data.db");
        assertTrue(db.delete());
        assertTrue(db.mkdirs());
        server.getPluginManager().enablePlugin(plugin);
        if (plugin.isEnabled()) {
            assertDoesNotThrow(() -> plugin.getStorageManager().getPlayer(UUID.randomUUID()),
                    "plugin stayed enabled with unusable storage");
        }
    }
}
