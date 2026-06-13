package dev.aponder.novarelics.relic;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.rarity.RarityDefinition;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RelicBuilder {

    public static final NamespacedKey RELIC_ID_KEY =
            new NamespacedKey("novarelics", "id");

    private final NovaRelics plugin;

    public RelicBuilder(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public ItemStack build(RelicDefinition def) {
        // Resolve base item (handles skull textures, ItemsAdder, Oraxen, Nexo, HeadDB)
        ItemStack item = plugin.getTextureManager().resolve(def);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            item = new ItemStack(def.getMaterial() != null ? def.getMaterial() : Material.PAPER);
            meta = item.getItemMeta();
        }

        // Display name
        if (def.getDisplayName() != null && !def.getDisplayName().isEmpty()) {
            meta.setDisplayName(MiniMsg.toLegacy(def.getDisplayName()));
        }

        // Lore: rarity prefix + custom lore
        List<String> loreLine = new ArrayList<>();
        RarityDefinition rarity = plugin.getRarityManager().getRarity(def.getRarity());
        if (rarity != null && rarity.getPrefix() != null && !rarity.getPrefix().isEmpty()) {
            loreLine.add(MiniMsg.toLegacy(rarity.getPrefix()));
        }
        for (String line : def.getLore()) {
            loreLine.add(MiniMsg.toLegacy(line));
        }
        meta.setLore(loreLine);

        // CustomModelData
        if (def.getCustomModelData() > 0) {
            meta.setCustomModelData(def.getCustomModelData());
        }

        // Glow: hidden enchant + HIDE_ENCHANTS flag
        if (def.isGlow() && def.getEnchantments().isEmpty()) {
            Enchantment unbreaking = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            if (unbreaking != null) meta.addEnchant(unbreaking, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        // Enchantments
        for (Map.Entry<Enchantment, Integer> entry : def.getEnchantments().entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        // Item flags
        for (ItemFlag flag : def.getItemFlags()) {
            meta.addItemFlags(flag);
        }

        // Persistent data — mark this item as a relic
        meta.getPersistentDataContainer().set(RELIC_ID_KEY, PersistentDataType.STRING, def.getId());

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isRelic(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(RELIC_ID_KEY, PersistentDataType.STRING);
    }

    public static String getRelicId(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(RELIC_ID_KEY, PersistentDataType.STRING);
    }
}
