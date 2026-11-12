package dev.aponder.novarelics.support;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicBuilder;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots MockBukkit and NovaRelics for one test.
 *
 * <p>By default the server reports a version the current allowlist accepts
 * ({@link CompatServerMock}) so behaviour can be tested on every MockBukkit generation.
 * The allowlist itself (NR-002) is tested separately with the real reported version.
 */
public abstract class PluginTestBase {

    protected ServerMock server;
    protected NovaRelics plugin;
    protected WorldMock world;

    @BeforeEach
    void bootServer() {
        server = MockBukkit.mock(new CompatServerMock());
        world = server.addSimpleWorld("world");
        beforePluginLoad();
        plugin = MockBukkit.load(NovaRelics.class);
        assertTrue(plugin.isEnabled(), "NovaRelics failed to enable under MockBukkit");
    }

    @AfterEach
    void stopServer() {
        MockBukkit.unmock();
    }

    /** Hook for tests that need to prepare the data folder before onEnable. */
    protected void beforePluginLoad() { }

    protected PlayerMock player() {
        PlayerMock p = server.addPlayer();
        p.setOp(false);
        return p;
    }

    protected PlayerMock op() {
        PlayerMock p = server.addPlayer();
        p.setOp(true);
        return p;
    }

    protected ItemStack relic(String id) {
        ItemStack item = plugin.getRelicManager().buildItem(id);
        if (item == null) throw new IllegalArgumentException("Unknown relic " + id);
        return item;
    }

    protected static String relicId(ItemStack item) {
        return RelicBuilder.getRelicId(item);
    }

    /** Writes a relic YAML into the plugin's relics folder and reloads the plugin. */
    protected void installRelic(String id, String yaml) {
        try {
            Path dir = plugin.getConfigManager().getRelicsFolder().toPath();
            Files.createDirectories(dir);
            Files.writeString(dir.resolve(id + ".yml"), yaml);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        plugin.reload();
    }

    /** Drains and returns every chat message the player received, colour codes stripped. */
    protected static List<String> messages(PlayerMock p) {
        List<String> out = new ArrayList<>();
        String m;
        while ((m = p.nextMessage()) != null) out.add(org.bukkit.ChatColor.stripColor(m));
        return out;
    }

    /** The registered listener instance of the given type (to call handlers with hand-built events). */
    protected <T> T listener(Class<T> type) {
        return org.bukkit.event.HandlerList.getRegisteredListeners(plugin).stream()
                .map(org.bukkit.plugin.RegisteredListener::getListener)
                .filter(type::isInstance).map(type::cast).findFirst()
                .orElseThrow(() -> new IllegalStateException("listener not registered: " + type));
    }

    protected static int count(org.bukkit.inventory.Inventory inv, String relicId) {
        int n = 0;
        for (ItemStack s : inv.getContents()) {
            if (s != null && relicId.equals(RelicBuilder.getRelicId(s))) n += s.getAmount();
        }
        return n;
    }
}
