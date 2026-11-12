package dev.aponder.novarelics.items;

import dev.aponder.novarelics.relic.RelicBuilder;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.support.PluginTestBase;
import dev.aponder.novarelics.util.ColorUtil;
import dev.aponder.novarelics.util.ItemUtil;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest extends PluginTestBase {

    @Test
    void builtRelicCarriesPersistentId() {
        ItemStack item = relic("teleport_crystal");
        assertEquals(Material.AMETHYST_SHARD, item.getType());
        assertEquals("teleport_crystal", item.getItemMeta().getPersistentDataContainer()
                .get(RelicBuilder.RELIC_ID_KEY, PersistentDataType.STRING));
        assertTrue(RelicBuilder.isRelic(item));
        assertSame(plugin.getRelicManager().getRelic("teleport_crystal"), plugin.getRelicManager().getRelicFromItem(item));
    }

    @Test
    void nonRelicsAreRejected() {
        assertFalse(RelicBuilder.isRelic(null));
        assertFalse(RelicBuilder.isRelic(new ItemStack(Material.AIR)));
        assertFalse(RelicBuilder.isRelic(new ItemStack(Material.STICK)));
        assertNull(RelicBuilder.getRelicId(new ItemStack(Material.STICK)));
        assertNull(plugin.getRelicManager().buildItem("does_not_exist"));
    }

    @Test
    void relicIdSurvivesCloneAndSerialization() {
        ItemStack item = relic("ore_charm");
        ItemStack copy = ItemStack.deserialize(item.serialize());
        assertEquals("ore_charm", RelicBuilder.getRelicId(copy));
        assertEquals("ore_charm", RelicBuilder.getRelicId(item.clone()));
    }

    @Test
    void forgedLookalikeWithoutTagIsNotARelic() {
        ItemStack real = relic("ore_charm");
        ItemStack fake = new ItemStack(real.getType());
        var meta = fake.getItemMeta();
        meta.setDisplayName(real.getItemMeta().getDisplayName());
        meta.setLore(real.getItemMeta().getLore());
        fake.setItemMeta(meta);
        assertFalse(RelicBuilder.isRelic(fake));
    }

    @Test
    void loreStartsWithRarityPrefixAndGlowHidesEnchant() {
        ItemStack item = relic("teleport_crystal");
        List<String> lore = item.getItemMeta().getLore();
        assertNotNull(lore);
        assertEquals("[Rare]", org.bukkit.ChatColor.stripColor(lore.get(0)));
        assertTrue(item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS));
        assertTrue(item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ATTRIBUTES));
    }

    @Test
    void customModelDataApplied() {
        RelicDefinition def = new RelicDefinition("cmd");
        def.setMaterial(Material.STICK);
        def.setCustomModelData(1234);
        ItemStack item = plugin.getRelicManager().buildItem(def);
        assertEquals(1234, item.getItemMeta().getCustomModelData());
    }

    @Test
    void airMaterialFallsBackToPaper() {
        RelicDefinition def = new RelicDefinition("air");
        def.setMaterial(Material.AIR);
        assertEquals(Material.PAPER, plugin.getRelicManager().buildItem(def).getType());
    }

    // ---------------------------------------------------------------- utils

    @Test
    void repairHelpers() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        Damageable d = (Damageable) sword.getItemMeta();
        d.setDamage(200);
        sword.setItemMeta(d);
        assertTrue(ItemUtil.isDamageable(sword));
        assertFalse(ItemUtil.isFullyRepaired(sword));
        assertEquals(0, ((Damageable) ItemUtil.fullyRepair(sword).getItemMeta()).getDamage());
        assertEquals(200, ((Damageable) sword.getItemMeta()).getDamage(), "original untouched");
        int afterHalf = ((Damageable) ItemUtil.repairByPercent(sword, 50).getItemMeta()).getDamage();
        assertEquals(Math.max(0, 200 - Material.IRON_SWORD.getMaxDurability() / 2), afterHalf);
        assertFalse(ItemUtil.isDamageable(null));
        assertEquals(Material.IRON_INGOT, ItemUtil.getSmeltResult(Material.RAW_IRON));
        assertNull(ItemUtil.getSmeltResult(Material.DIAMOND));
    }

    @Test
    void colorUtil() {
        assertEquals(0xFF6600, ColorUtil.toBukkitColor("#FF6600").asRGB());
        assertEquals(org.bukkit.Color.WHITE, ColorUtil.toBukkitColor("zzz"));
        assertTrue(ColorUtil.isValidHex("#a1B2c3"));
        assertFalse(ColorUtil.isValidHex("#12345"));
        assertEquals("#FF6600", ColorUtil.toHex(org.bukkit.Color.fromRGB(0xFF6600)));
    }

    @Test
    void miniMessageBasicsToLegacy() {
        assertEquals("§cMini", MiniMsg.toLegacy("<red>Mini</red>"));
        assertEquals("Fish & Chips", MiniMsg.toLegacy("Fish & Chips"));
        assertEquals("", MiniMsg.toLegacy(""));
        assertEquals("abc", MiniMsg.strip("<bold>abc</bold>"));
    }

    static List<String> hexInputs() {
        return List.of("<gradient:#FF6600:#FFDD00>Phoenix Feather</gradient>", "<#FF6600>Orange", "&#FF6600Orange");
    }

    /**
     * Guard, not a regression: on Paper (and MockBukkit, which mirrors it) Adventure's
     * legacySection() is supplied by the server with hex support, so hex survives.
     * Outside Paper (plain Adventure, e.g. Spigot after NR-003 is fixed) it is down-sampled,
     * which is why NR-009's fix must configure the serializer explicitly.
     */
    @ParameterizedTest(name = "hex colour kept on Paper: {0}")
    @MethodSource("hexInputs")
    void hexColoursAreNotDownsampled(String input) {
        String legacy = MiniMsg.toLegacy(input);
        assertTrue(legacy.contains("§x"), () -> "expected §x§R§R§G§G§B§B hex sequence, got: " + legacy.replace('§', '&'));
    }

    @Test
    @Tag("regression")
    @DisplayName("NR-009 legacy & colour codes are translated, not shown literally")
    void ampersandCodesTranslated() {
        assertEquals("§aGreen", MiniMsg.toLegacy("&aGreen"));
    }
}
