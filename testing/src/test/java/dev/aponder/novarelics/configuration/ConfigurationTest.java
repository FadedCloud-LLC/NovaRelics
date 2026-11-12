package dev.aponder.novarelics.configuration;

import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.aura.AuraType;
import dev.aponder.novarelics.cooldown.CooldownType;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest extends PluginTestBase {

    private File relicsDir() { return plugin.getConfigManager().getRelicsFolder(); }

    private RelicDefinition loadYaml(String name, String yaml) throws Exception {
        Path f = relicsDir().toPath().resolve(name);
        Files.writeString(f, yaml);
        return plugin.getRelicManager().getLoader().load(f.toFile());
    }

    // ---------------------------------------------------------------- config.yml / language

    @Test
    void defaultConfigValues() {
        var cfg = plugin.getConfigManager().getMainConfig();
        assertEquals("SQLITE", cfg.getString("storage.type"));
        assertEquals("MEMORY", cfg.getString("cooldown.storage"));
        assertEquals(64, cfg.getInt("performance.max-vein-mine-blocks"));
        assertFalse(plugin.getConfigManager().isDebug());
    }

    @Test
    void messagesReplacePrefixAndPlaceholders() {
        String msg = plugin.getConfigManager().getMessage("unknown-relic", "<relic>", "abc");
        assertFalse(msg.contains("<prefix>"));
        assertTrue(msg.contains("abc"));
        assertTrue(msg.contains("NovaRelics"));
    }

    @Test
    void missingMessageKeyFallsBackToKey() {
        assertEquals("definitely-not-a-key", plugin.getConfigManager().getMessage("definitely-not-a-key"));
    }

    @ParameterizedTest(name = "language {0}")
    @ValueSource(strings = {"es", "ru"})
    void bundledLanguagesLoad(String lang) {
        plugin.getConfigManager().getMainConfig().set("language", lang);
        plugin.getConfig().set("language", lang);
        plugin.saveConfig();
        plugin.reload();
        assertTrue(new File(plugin.getDataFolder(), "language/" + lang + ".yml").isFile());
        String english = "This relic is on cooldown";
        assertFalse(plugin.getConfigManager().getMessage("cooldown-active").contains(english),
                "translated message expected for " + lang);
    }

    @Test
    void unknownLanguageFallsBackToEnglish() {
        plugin.getConfig().set("language", "xx");
        plugin.saveConfig();
        plugin.reload();
        assertTrue(plugin.getConfigManager().getMessage("no-permission").contains("permission"));
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-036 a message key deleted from a language file falls back instead of printing the raw key")
    void translationMissingAKeyFallsBackToEnglish() throws Exception {
        plugin.getConfig().set("language", "es");
        plugin.saveConfig();
        plugin.reload();
        Path es = plugin.getDataFolder().toPath().resolve("language/es.yml");
        List<String> lines = Files.readAllLines(es);
        Files.write(es, lines.stream().filter(l -> !l.startsWith("no-permission:")).toList());
        plugin.reload();
        String msg = plugin.getConfigManager().getMessage("no-permission");
        assertNotEquals("no-permission", msg, "falls back instead of printing the raw key");
    }

    @Test
    void corruptConfigStillEnablesWithDefaults() throws Exception {
        Files.writeString(plugin.getDataFolder().toPath().resolve("config.yml"), "storage: [unclosed\n  ::: !!");
        plugin.reload();
        assertEquals("en", plugin.getConfigManager().getMainConfig().getString("language", "en"));
        assertTrue(plugin.getRelicManager().getCount() > 0);
    }

    // ---------------------------------------------------------------- rarities

    @Test
    void bundledRaritiesLoaded() {
        var ids = plugin.getRarityManager().getRarities().stream().map(r -> r.getId()).toList();
        assertEquals(List.of("common", "uncommon", "rare", "epic", "legendary", "mythic", "divine"), ids);
        assertEquals("common", plugin.getRarityManager().getRarity("unknown").getId(), "unknown rarity falls back to common");
    }

    @Test
    void emptyRaritiesFileUsesDefaults() throws Exception {
        Files.writeString(plugin.getDataFolder().toPath().resolve("rarities.yml"), "rarities: {}\n");
        plugin.reload();
        assertEquals(7, plugin.getRarityManager().getRarities().size());
    }

    // ---------------------------------------------------------------- relic loader

    @Test
    void loaderParsesAllSections() throws Exception {
        RelicDefinition def = loadYaml("full.yml", """
                id: Full_Relic
                display-name: "<red>Full"
                material: diamond_sword
                rarity: EPIC
                custom-model-data: 42
                glow: true
                permission: test.use
                category: weapons
                max-charges: 3
                lore: ["a", "b"]
                enchantments:
                  sharpness: 2
                item-flags: [HIDE_ATTRIBUTES, NOT_A_FLAG]
                aura:
                  enabled: true
                  type: fire_effect
                  particle: FLAME
                  radius: 1.5
                  update-ticks: 7
                  only-when-held: true
                triggers:
                  RIGHT_CLICK:
                    - type: heal
                      amount: 4
                    - amount: 5
                  NOT_A_TRIGGER:
                    - type: HEAL
                cooldown:
                  type: global
                  duration: 12
                  shared-key: k
                """);
        assertEquals("full_relic", def.getId());
        assertEquals(Material.DIAMOND_SWORD, def.getMaterial());
        assertEquals("epic", def.getRarity());
        assertEquals(42, def.getCustomModelData());
        assertTrue(def.isGlow());
        assertEquals("test.use", def.getPermission());
        assertEquals(3, def.getMaxCharges());
        assertEquals(List.of("a", "b"), def.getLore());
        assertEquals(List.of(ItemFlag.HIDE_ATTRIBUTES), def.getItemFlags(), "unknown flags ignored");
        assertTrue(def.getAura().isEnabled());
        assertEquals(AuraType.FIRE_EFFECT, def.getAura().getType());
        assertEquals(7, def.getAura().getUpdateTicks());
        assertEquals(1, def.getTriggers().size(), "unknown trigger ignored");
        List<AbilityConfig> abilities = def.getAbilitiesForTrigger(TriggerType.RIGHT_CLICK);
        assertEquals(1, abilities.size(), "ability without type skipped");
        assertEquals("HEAL", abilities.get(0).getType());
        assertEquals(4, abilities.get(0).getInt("amount", 0));
        assertEquals(CooldownType.GLOBAL, def.getCooldownType());
        assertEquals(12, def.getCooldownSeconds());
        assertEquals("k", def.getCooldownSharedKey());
    }

    @Test
    void loaderDefaultsForMinimalFile() throws Exception {
        RelicDefinition def = loadYaml("Minimal Thing.yml", "lore: []\n");
        assertEquals("minimal_thing", def.getId(), "id derived from file name");
        assertEquals(Material.PAPER, def.getMaterial());
        assertEquals("common", def.getRarity());
        assertEquals(CooldownType.PER_PLAYER, def.getCooldownType());
        assertFalse(def.getAura().isEnabled());
    }

    @Test
    void loaderToleratesWrongTypesAndUnknownMaterial() throws Exception {
        RelicDefinition def = loadYaml("odd.yml", """
                material: NOT_A_MATERIAL
                custom-model-data: "abc"
                cooldown:
                  type: SIDEWAYS
                  duration: "ten"
                aura:
                  type: NOPE
                  radius: "big"
                """);
        assertEquals(Material.PAPER, def.getMaterial());
        assertEquals(0, def.getCustomModelData());
        assertEquals(CooldownType.PER_PLAYER, def.getCooldownType());
        assertEquals(0, def.getCooldownSeconds());
        assertEquals(AuraType.CUSTOM_PARTICLE_AURA, def.getAura().getType());
    }

    @Test
    void invalidYamlRelicIsSkippedWithoutBreakingOthers() throws Exception {
        Files.writeString(relicsDir().toPath().resolve("broken.yml"), "triggers: [ : :\n  - {");
        plugin.reload();
        assertNotNull(plugin.getRelicManager().getRelic("teleport_crystal"));
    }

    @Test
    void abilityConfigAccessorsHandleBadValues() {
        AbilityConfig c = new AbilityConfig("heal", java.util.Map.of("i", "x", "d", "1.5", "b", "TRUE", "s", 7));
        assertEquals("HEAL", c.getType());
        assertEquals(9, c.getInt("i", 9));
        assertEquals(1.5, c.getDouble("d", 0));
        assertTrue(c.getBoolean("b", false));
        assertEquals("7", c.getString("s", null));
        assertEquals("def", c.getString("missing", "def"));
    }

    // ---------------------------------------------------------------- regressions

    static List<String> bundledRelicFiles() {
        return List.of("angler_charm", "berserker_blood", "gravity_gem", "harvest_charm", "mending_wool", "ore_charm",
                "phoenix_feather", "shadow_shroud", "storm_caller", "swift_talisman", "teleport_crystal",
                "vampire_fang", "vitality_locket", "void_dagger", "web_weaver");
    }

    @ParameterizedTest(name = "NR-008 round-trip {0}")
    @Tag("regression")
    @org.junit.jupiter.params.provider.MethodSource("bundledRelicFiles")
    @DisplayName("NR-008 saving a relic must not lose configuration")
    void saveLoadRoundTripPreservesEverything(String id) throws Exception {
        Path tmp = Files.createTempDirectory("nr-roundtrip");
        Path original = tmp.resolve(id + ".yml");
        try (InputStream in = plugin.getResource("relics/" + id + ".yml")) {
            Files.copy(in, original);
        }
        var loader = plugin.getRelicManager().getLoader();
        RelicDefinition before = loader.load(original.toFile());
        Path saved = tmp.resolve(id + "-saved.yml");
        assertTrue(loader.save(before, saved.toFile()));
        RelicDefinition after = loader.load(saved.toFile());

        assertAll(
                () -> assertEquals(before.getDisplayName(), after.getDisplayName(), "display-name"),
                () -> assertEquals(before.getMaterial(), after.getMaterial(), "material"),
                () -> assertEquals(before.getLore(), after.getLore(), "lore"),
                () -> assertEquals(before.isGlow(), after.isGlow(), "glow"),
                () -> assertEquals(before.getItemFlags(), after.getItemFlags(), "item-flags"),
                () -> assertEquals(before.getEnchantments(), after.getEnchantments(), "enchantments"),
                () -> assertEquals(before.getMaxCharges(), after.getMaxCharges(), "max-charges"),
                () -> assertEquals(before.getAura().isEnabled(), after.getAura().isEnabled(), "aura.enabled"),
                () -> assertEquals(before.getAura().getType(), after.getAura().getType(), "aura.type"),
                () -> assertEquals(before.getAura().getParticle(), after.getAura().getParticle(), "aura.particle"),
                () -> assertEquals(before.getAura().getRadius(), after.getAura().getRadius(), "aura.radius"),
                () -> assertEquals(before.getTriggers().keySet(), after.getTriggers().keySet(), "triggers"),
                () -> assertEquals(before.getCooldownSeconds(), after.getCooldownSeconds(), "cooldown"));
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-028 first run installs all 15 bundled relics")
    void firstRunInstallsAllBundledRelics() {
        String[] files = relicsDir().list((d, n) -> n.endsWith(".yml"));
        assertNotNull(files);
        assertEquals(15, files.length, () -> "installed: " + String.join(", ", files));
    }
}
