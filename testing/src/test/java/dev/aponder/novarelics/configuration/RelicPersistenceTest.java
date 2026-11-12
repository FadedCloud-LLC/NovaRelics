package dev.aponder.novarelics.configuration;

import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.relic.RelicIds;
import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.support.Relics;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Saving, exporting, duplicating and importing relics (fixes for NR-008 and NR-012). */
class RelicPersistenceTest extends PluginTestBase {

    private Path relics() { return plugin.getConfigManager().getRelicsFolder().toPath(); }

    @Test
    @DisplayName("NR-008 export writes a complete copy to exports/ and leaves the live file untouched")
    void exportDoesNotTouchLiveFile() throws Exception {
        Path live = relics().resolve("teleport_crystal.yml");
        String before = Files.readString(live);
        server.dispatchCommand(op(), "nr export teleport_crystal");
        assertEquals(before, Files.readString(live), "live relic file must not change");
        File exported = new File(plugin.getDataFolder(), "exports/teleport_crystal.yml");
        assertTrue(exported.isFile());
        YamlConfiguration y = YamlConfiguration.loadConfiguration(exported);
        assertTrue(y.getBoolean("aura.enabled"), "aura exported");
        assertEquals("CIRCLE_EFFECT", y.getString("aura.type"));
        assertTrue(y.getStringList("item-flags").contains("HIDE_ENCHANTS"));
    }

    @Test
    @DisplayName("NR-008 saving keeps comments and keys the plugin does not manage")
    void saveKeepsCommentsAndUnknownKeys() throws Exception {
        Files.writeString(relics().resolve("handmade.yml"), """
                # Hand-written relic — keep this comment
                id: handmade
                display-name: "Old Name"
                material: STICK
                my-custom-section:
                  answer: 42
                """);
        plugin.reload();
        RelicDefinition def = plugin.getRelicManager().getRelic("handmade");
        def.setDisplayName("New Name");
        assertTrue(plugin.getRelicManager().save(def));
        String text = Files.readString(relics().resolve("handmade.yml"));
        assertTrue(text.contains("keep this comment"), text);
        assertTrue(text.contains("answer: 42"), text);
        assertTrue(text.contains("New Name"), text);
    }

    @Test
    @DisplayName("NR-008 editor toggles (aura) are persisted")
    void auraTogglePersists() {
        RelicDefinition def = plugin.getRelicManager().getRelic("ore_charm");
        boolean before = def.getAura().isEnabled();
        def.getAura().setEnabled(!before);
        plugin.getRelicManager().save(def);
        plugin.reload();
        assertEquals(!before, plugin.getRelicManager().getRelic("ore_charm").getAura().isEnabled());
    }

    @Test
    @DisplayName("NR-008 duplicate copies every field including the aura")
    void duplicateIsDeep() {
        RelicDefinition copy = plugin.getRelicManager().duplicate("teleport_crystal", "teleport_crystal_copy");
        RelicDefinition src = plugin.getRelicManager().getRelic("teleport_crystal");
        assertNotNull(copy);
        assertEquals("teleport_crystal_copy", copy.getId());
        assertEquals(src.getAura().getType(), copy.getAura().getType());
        assertTrue(copy.getAura().isEnabled());
        assertEquals(src.getTriggers().keySet(), copy.getTriggers().keySet());
        assertNotSame(src.getLore(), copy.getLore(), "independent lists");
        assertTrue(relics().resolve("teleport_crystal_copy.yml").toFile().isFile());
        assertNull(plugin.getRelicManager().duplicate("teleport_crystal", "../evil"), "invalid id refused");
    }

    @Test
    void enchantmentsAcceptKeysAndLegacyNames() throws Exception {
        installRelic("enchanted", """
                id: enchanted
                material: DIAMOND_SWORD
                enchantments:
                  sharpness: 3
                  minecraft:unbreaking: 2
                """);
        RelicDefinition def = plugin.getRelicManager().getRelic("enchanted");
        assertEquals(2, def.getEnchantments().size(), def.getEnchantments()::toString);
        plugin.getRelicManager().save(def);
        plugin.reload();
        assertEquals(2, plugin.getRelicManager().getRelic("enchanted").getEnchantments().size(), "round-trips");
    }

    @ParameterizedTest(name = "id \"{0}\" valid={1}")
    @org.junit.jupiter.params.provider.CsvSource({"ore_charm,true", "a-b_9,true", "../x,false", "a/b,false",
            "UPPER,false", "'',false", "with space,false"})
    void idRules(String id, boolean valid) {
        assertEquals(valid, RelicIds.isValid(id));
    }

    @Test
    @DisplayName("NR-012 a relic file whose id would escape the folder is not loaded")
    void loaderSkipsUnsafeIds() throws Exception {
        Files.writeString(relics().resolve("sneaky.yml"), Relics.probe("../../sneaky", 0, "SNEAK"));
        plugin.reload();
        assertTrue(plugin.getRelicManager().getRelicIds().stream().noneMatch(id -> id.contains("..")));
    }

    @ParameterizedTest(name = "NR-012 import rejects \"{0}\"")
    @ValueSource(strings = {"../config", "..\\config", "language/en"})
    void importRejectsPathsOutsideRelics(String name) {
        PlayerMock admin = op();
        server.dispatchCommand(admin, "nr import " + name);
        assertTrue(messages(admin).stream().anyMatch(m -> m.contains("Invalid relic id")));
    }

    @Test
    void createRejectsInvalidIdWithMessage() {
        PlayerMock admin = op();
        server.dispatchCommand(admin, "nr create Bad/Id");
        assertTrue(messages(admin).stream().anyMatch(m -> m.contains("Invalid relic id")));
    }
}
