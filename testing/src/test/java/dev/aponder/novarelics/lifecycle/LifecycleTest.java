package dev.aponder.novarelics.lifecycle;

import dev.aponder.novarelics.command.NovaRelicsCommand;
import dev.aponder.novarelics.support.PluginTestBase;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LifecycleTest extends PluginTestBase {

    @Test
    void enablesAndWiresAllServices() {
        assertNotNull(plugin.getConfigManager());
        assertNotNull(plugin.getStorageManager());
        assertNotNull(plugin.getRelicManager());
        assertNotNull(plugin.getAbilityRegistry());
        assertNotNull(plugin.getCooldownManager());
        assertNotNull(plugin.getAuraManager());
        assertEquals(50, plugin.getAbilityRegistry().getTypes().size(), "registered abilities");
    }

    @Test
    void registersCommandWithAliases() {
        var cmd = plugin.getCommand("novarelics");
        assertNotNull(cmd);
        assertInstanceOf(NovaRelicsCommand.class, cmd.getExecutor());
        assertTrue(cmd.getAliases().containsAll(List.of("nr", "relics")));
    }

    @Test
    void registersTriggerAndGuiListeners() {
        // 22 trigger listeners + ChatInputManager + KeptItems (NR-007)
        long listenerObjects = HandlerList.getRegisteredListeners(plugin).stream()
                .map(l -> l.getListener()).distinct().count();
        assertEquals(24, listenerObjects);
    }

    @Test
    void createsDefaultFiles() {
        File data = plugin.getDataFolder();
        assertTrue(new File(data, "config.yml").isFile());
        assertTrue(new File(data, "rarities.yml").isFile());
        assertTrue(new File(data, "language/en.yml").isFile());
        assertTrue(new File(data, "data.db").isFile(), "SQLite database created");
    }

    @Test
    void startsAuraTaskAndCancelsItOnDisable() {
        List<BukkitTask> before = server.getScheduler().getPendingTasks().stream()
                .filter(t -> t.getOwner() == plugin).toList();
        assertFalse(before.isEmpty(), "aura task scheduled");

        server.getPluginManager().disablePlugin(plugin);

        assertFalse(plugin.isEnabled());
        assertTrue(server.getScheduler().getPendingTasks().stream()
                .noneMatch(t -> t.getOwner() == plugin && !t.isCancelled()), "no live tasks after disable");
        assertTrue(HandlerList.getRegisteredListeners(plugin).isEmpty(), "listeners unregistered");
    }

    @Test
    void reloadDoesNotDuplicateListenersOrTasks() {
        int listeners = HandlerList.getRegisteredListeners(plugin).size();
        for (int i = 0; i < 25; i++) plugin.reload();
        assertEquals(listeners, HandlerList.getRegisteredListeners(plugin).size());
        long live = server.getScheduler().getPendingTasks().stream()
                .filter(t -> t.getOwner() == plugin && !t.isCancelled()).count();
        assertEquals(1, live, "exactly one aura task after repeated reloads");
    }

    @Test
    void loadsBundledRelicsOnFirstRun() {
        assertTrue(plugin.getRelicManager().getCount() >= 5);
        assertNotNull(plugin.getRelicManager().getRelic("teleport_crystal"));
    }
}
