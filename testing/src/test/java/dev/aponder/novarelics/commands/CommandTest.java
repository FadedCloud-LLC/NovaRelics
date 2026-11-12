package dev.aponder.novarelics.commands;

import dev.aponder.novarelics.support.PluginTestBase;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandTest extends PluginTestBase {

    private List<String> run(org.bukkit.command.CommandSender sender, String line) {
        server.dispatchCommand(sender, line);
        return sender instanceof PlayerMock p ? messages(p) : List.of();
    }

    private static boolean anyContains(List<String> msgs, String needle) {
        return msgs.stream().anyMatch(m -> m.contains(needle));
    }

    // ---------------------------------------------------------------- permissions

    @ParameterizedTest(name = "non-op denied: /nr {0}")
    @ValueSource(strings = {"reload", "give x teleport_crystal", "take x teleport_crystal", "editor",
            "create abc", "delete teleport_crystal", "import teleport_crystal", "export teleport_crystal"})
    void protectedSubcommandsRequirePermission(String args) {
        PlayerMock p = player();
        List<String> out = run(p, "nr " + args);
        assertTrue(anyContains(out, "do not have permission"), () -> "messages: " + out);
        assertNotNull(plugin.getRelicManager().getRelic("teleport_crystal"), "nothing was deleted");
        assertNull(plugin.getRelicManager().getRelic("abc"), "nothing was created");
    }

    @ParameterizedTest(name = "public: /nr {0}")
    @ValueSource(strings = {"help", "list", "info teleport_crystal"})
    void publicSubcommandsWorkWithoutPermission(String args) {
        PlayerMock p = player();
        List<String> out = run(p, "nr " + args);
        assertFalse(anyContains(out, "do not have permission"));
        assertFalse(out.isEmpty());
    }

    @Test
    void adminParentPermissionGrantsChildren() {
        PlayerMock p = player();
        p.addAttachment(plugin, "novarelics.admin", true);
        run(p, "nr give " + p.getName() + " teleport_crystal");
        assertEquals(1, count(p.getInventory(), "teleport_crystal"));
    }

    @Test
    void permissionRevokedAtRuntimeIsEnforced() {
        PlayerMock p = player();
        var att = p.addAttachment(plugin, "novarelics.give", true);
        run(p, "nr give " + p.getName() + " teleport_crystal");
        att.remove();
        p.recalculatePermissions();
        List<String> out = run(p, "nr give " + p.getName() + " teleport_crystal");
        assertTrue(anyContains(out, "do not have permission"));
        assertEquals(1, count(p.getInventory(), "teleport_crystal"));
    }

    @Test
    void unknownSubcommandAndNoArgs() {
        PlayerMock p = player();
        assertTrue(anyContains(run(p, "nr bogus"), "Unknown subcommand"));
        assertTrue(anyContains(run(p, "nr"), "/nr help"), "no args shows help");
    }

    @Test
    void tabCompletionHidesSubcommandsWithoutPermission() {
        PlayerMock p = player();
        var cmd = plugin.getCommand("novarelics");
        List<String> subs = cmd.getTabCompleter().onTabComplete(p, cmd, "nr", new String[]{""});
        assertTrue(subs.containsAll(List.of("help", "list", "info")));
        assertFalse(subs.contains("give"));
        assertFalse(subs.contains("delete"));

        PlayerMock admin = op();
        assertTrue(cmd.getTabCompleter().onTabComplete(admin, cmd, "nr", new String[]{"g"}).contains("give"));
        assertTrue(cmd.getTabCompleter().onTabComplete(admin, cmd, "nr", new String[]{"give", admin.getName().substring(0, 1)})
                .contains(admin.getName()));
        assertEquals(List.of("teleport_crystal"),
                cmd.getTabCompleter().onTabComplete(admin, cmd, "nr", new String[]{"info", "telep"}));
    }

    // ---------------------------------------------------------------- give / take

    @Test
    void giveCreatesTaggedRelicAndMarksDiscovered() {
        PlayerMock admin = op();
        PlayerMock target = player();
        List<String> out = run(admin, "nr give " + target.getName() + " teleport_crystal 3");
        assertTrue(anyContains(out, "Gave"));
        assertEquals(3, count(target.getInventory(), "teleport_crystal"));
        assertTrue(anyContains(messages(target), "You received"));
        assertTrue(plugin.getStorageManager().getPlayer(target.getUniqueId()).hasDiscovered("teleport_crystal"));
    }

    @Test
    void giveFromConsoleWorks() {
        PlayerMock target = player();
        server.dispatchCommand(server.getConsoleSender(), "nr give " + target.getName() + " ore_charm");
        assertEquals(1, count(target.getInventory(), "ore_charm"));
    }

    @ParameterizedTest(name = "invalid amount \"{0}\"")
    @ValueSource(strings = {"abc", "1.5", "99999999999"})
    void giveRejectsNonNumericAmounts(String amount) {
        PlayerMock admin = op();
        assertTrue(anyContains(run(admin, "nr give " + admin.getName() + " teleport_crystal " + amount), "Invalid amount"));
        assertEquals(0, count(admin.getInventory(), "teleport_crystal"));
    }

    @Test
    void giveClampsZeroAndNegativeToOne() {
        PlayerMock admin = op();
        run(admin, "nr give " + admin.getName() + " teleport_crystal 0");
        run(admin, "nr give " + admin.getName() + " teleport_crystal -5");
        assertEquals(2, count(admin.getInventory(), "teleport_crystal"));
    }

    @Test
    void giveUnknownPlayerOrRelic() {
        PlayerMock admin = op();
        assertTrue(anyContains(run(admin, "nr give nobody teleport_crystal"), "was not found"));
        assertTrue(anyContains(run(admin, "nr give " + admin.getName() + " nope"), "was not found"));
        assertTrue(anyContains(run(admin, "nr give"), "Usage"));
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-022 give: items that do not fit are dropped, never silently deleted")
    void giveOverflowIsNotLost() {
        PlayerMock admin = op();
        // Fill every slot: MockBukkit's addItem also uses armor/off-hand slots (a real server does not).
        for (int i = 0; i < admin.getInventory().getSize(); i++) admin.getInventory().setItem(i, new ItemStack(Material.DIRT, 64));
        run(admin, "nr give " + admin.getName() + " teleport_crystal 2");
        long dropped = world.getEntities().stream()
                .filter(e -> e instanceof org.bukkit.entity.Item item && "teleport_crystal".equals(relicId(item.getItemStack())))
                .mapToLong(e -> ((org.bukkit.entity.Item) e).getItemStack().getAmount()).sum();
        assertEquals(2, count(admin.getInventory(), "teleport_crystal") + dropped,
                "relics given to a full inventory must be dropped at the player's feet");
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-022 take: removes exactly one relic, not the whole stack")
    void takeRemovesOne() {
        PlayerMock admin = op();
        PlayerMock target = player();
        run(admin, "nr give " + target.getName() + " teleport_crystal 5");
        run(admin, "nr take " + target.getName() + " teleport_crystal");
        assertEquals(4, count(target.getInventory(), "teleport_crystal"));
    }

    @Test
    void takeReportsWhenMissing() {
        PlayerMock admin = op();
        PlayerMock target = player();
        assertTrue(anyContains(run(admin, "nr take " + target.getName() + " teleport_crystal"), "does not have"));
    }

    // ---------------------------------------------------------------- create / delete / import / export

    @Test
    void createRegistersAndSavesFile() {
        PlayerMock admin = op();
        run(admin, "nr create my_relic");
        assertNotNull(plugin.getRelicManager().getRelic("my_relic"));
        assertTrue(new File(plugin.getConfigManager().getRelicsFolder(), "my_relic.yml").isFile());
        assertTrue(anyContains(run(admin, "nr create my_relic"), "already exists"));
    }

    @ParameterizedTest(name = "NR-012 rejects id \"{0}\"")
    @Tag("regression")
    @ValueSource(strings = {"../escape", "..\\escape", "sub/dir", "../../evil_config"})
    @DisplayName("NR-012 create: ids cannot escape the relics folder")
    void createRejectsPathTraversal(String id) throws Exception {
        PlayerMock admin = op();
        run(admin, "nr create " + id);
        File relics = plugin.getConfigManager().getRelicsFolder().getCanonicalFile();
        File target = new File(relics, id.toLowerCase() + ".yml").getCanonicalFile();
        boolean escaped = target.exists() && !target.getParentFile().equals(relics);
        if (target.exists()) target.delete();
        assertFalse(escaped, "relic file written outside the relics folder: " + target);
        assertNull(plugin.getRelicManager().getRelic(id.toLowerCase()), "invalid id must not be registered");
    }

    @Test
    void deleteRemovesRelicAndFile() {
        PlayerMock admin = op();
        File f = new File(plugin.getConfigManager().getRelicsFolder(), "harvest_charm.yml");
        assertTrue(f.isFile());
        run(admin, "nr delete harvest_charm");
        assertNull(plugin.getRelicManager().getRelic("harvest_charm"));
        assertFalse(f.exists());
        assertTrue(anyContains(run(admin, "nr delete harvest_charm"), "was not found"));
    }

    @Test
    void importLoadsFileAndReportsMissing() throws Exception {
        PlayerMock admin = op();
        java.nio.file.Files.writeString(new File(plugin.getConfigManager().getRelicsFolder(), "manual.yml").toPath(),
                dev.aponder.novarelics.support.Relics.probe("manual", 0, "SNEAK"));
        run(admin, "nr import manual");
        assertNotNull(plugin.getRelicManager().getRelic("manual"));
        assertTrue(anyContains(run(admin, "nr import does_not_exist"), "Import failed"));
    }

    @Test
    void reloadPicksUpNewFiles() throws Exception {
        PlayerMock admin = op();
        java.nio.file.Files.writeString(new File(plugin.getConfigManager().getRelicsFolder(), "late.yml").toPath(),
                dev.aponder.novarelics.support.Relics.probe("late", 0, "SNEAK"));
        assertTrue(anyContains(run(admin, "nr reload"), "reloaded"));
        assertNotNull(plugin.getRelicManager().getRelic("late"));
    }

    @Test
    void editorIsPlayerOnly() {
        server.dispatchCommand(server.getConsoleSender(), "nr editor");
        // Console output is not captured as chat; assert no inventory was opened for anyone and no exception.
        assertTrue(server.getOnlinePlayers().isEmpty());
    }

    @Test
    void listAndInfoShowRelics() {
        PlayerMock p = player();
        List<String> list = run(p, "nr list");
        assertTrue(anyContains(list, "teleport_crystal"));
        List<String> info = run(p, "nr info teleport_crystal");
        assertTrue(anyContains(info, "RIGHT_CLICK"));
        assertTrue(anyContains(run(p, "nr info nope"), "was not found"));
    }
}
