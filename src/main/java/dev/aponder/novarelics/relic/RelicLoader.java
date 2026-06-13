package dev.aponder.novarelics.relic;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraMode;
import dev.aponder.novarelics.aura.AuraType;
import dev.aponder.novarelics.cooldown.CooldownType;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class RelicLoader {

    private final NovaRelics plugin;

    public RelicLoader(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public List<RelicDefinition> loadAll() {
        List<RelicDefinition> loaded = new ArrayList<>();
        File folder = plugin.getConfigManager().getRelicsFolder();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return loaded;

        for (File file : files) {
            try {
                RelicDefinition def = load(file);
                if (def != null) loaded.add(def);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to load relic from " + file.getName(), e);
            }
        }
        return loaded;
    }

    public RelicDefinition load(File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String id = cfg.getString("id");
        if (id == null || id.isBlank()) {
            id = file.getName().replace(".yml", "").toLowerCase().replace(" ", "_");
        }

        RelicDefinition def = new RelicDefinition(id.toLowerCase());

        def.setDisplayName(cfg.getString("display-name", id));
        def.setLore(cfg.getStringList("lore"));

        String matStr = cfg.getString("material", "PAPER");
        Material mat = Material.matchMaterial(matStr);
        def.setMaterial(mat != null ? mat : Material.PAPER);

        def.setRarity(cfg.getString("rarity", "common").toLowerCase());
        def.setCustomModelData(cfg.getInt("custom-model-data", 0));
        def.setGlow(cfg.getBoolean("glow", false));
        def.setSkullTexture(cfg.getString("skull-texture", null));
        def.setHeadDatabaseId(cfg.getString("head-database-id", null));
        def.setItemsAdderId(cfg.getString("itemsadder-id", null));
        def.setOraxenId(cfg.getString("oraxen-id", null));
        def.setNexoId(cfg.getString("nexo-id", null));
        def.setPermission(cfg.getString("permission", ""));
        def.setCategory(cfg.getString("category", "general"));
        def.setMaxCharges(cfg.getInt("max-charges", -1));

        // Enchantments
        ConfigurationSection enchSection = cfg.getConfigurationSection("enchantments");
        if (enchSection != null) {
            Map<Enchantment, Integer> enchMap = new LinkedHashMap<>();
            for (String key : enchSection.getKeys(false)) {
                Enchantment ench = Enchantment.getByName(key.toUpperCase());
                if (ench != null) {
                    enchMap.put(ench, enchSection.getInt(key, 1));
                }
            }
            def.setEnchantments(enchMap);
        }

        // Item flags
        List<String> flagStrings = cfg.getStringList("item-flags");
        List<ItemFlag> flags = new ArrayList<>();
        for (String fs : flagStrings) {
            try { flags.add(ItemFlag.valueOf(fs.toUpperCase())); }
            catch (IllegalArgumentException ignored) {}
        }
        def.setItemFlags(flags);

        // Triggers + abilities
        ConfigurationSection trigSection = cfg.getConfigurationSection("triggers");
        if (trigSection != null) {
            Map<TriggerType, List<AbilityConfig>> trigMap = new EnumMap<>(TriggerType.class);
            for (String trigKey : trigSection.getKeys(false)) {
                TriggerType trigType = TriggerType.fromString(trigKey);
                if (trigType == null) continue;

                List<Map<?, ?>> abilityList = trigSection.getMapList(trigKey);
                List<AbilityConfig> abilities = new ArrayList<>();
                for (Map<?, ?> rawMap : abilityList) {
                    Map<String, Object> params = new LinkedHashMap<>();
                    rawMap.forEach((k, v) -> params.put(k.toString(), v));
                    String type = (String) params.remove("type");
                    if (type == null) continue;
                    abilities.add(new AbilityConfig(type, params));
                }
                if (!abilities.isEmpty()) trigMap.put(trigType, abilities);
            }
            def.setTriggers(trigMap);
        }

        // Aura
        ConfigurationSection auraSection = cfg.getConfigurationSection("aura");
        if (auraSection != null) {
            AuraDefinition aura = new AuraDefinition();
            aura.setEnabled(auraSection.getBoolean("enabled", false));
            aura.setType(AuraType.fromString(auraSection.getString("type")));
            aura.setMode(AuraMode.fromString(auraSection.getString("mode")));
            aura.setParticle(auraSection.getString("particle", "ENCHANT"));
            aura.setColor(auraSection.getString("color", null));
            aura.setFromColor(auraSection.getString("from-color", null));
            aura.setToColor(auraSection.getString("to-color", null));
            aura.setDustSize((float) auraSection.getDouble("size", 1.0));
            aura.setRadius(auraSection.getDouble("radius", 1.0));
            aura.setSpeed(auraSection.getDouble("speed", 0.05));
            aura.setAmount(auraSection.getInt("amount", 10));
            aura.setUpdateTicks(auraSection.getInt("update-ticks", 5));
            aura.setOnlyWhenHeld(auraSection.getBoolean("only-when-held", false));
            aura.setOffsetX(auraSection.getDouble("offset-x", 0.2));
            aura.setOffsetY(auraSection.getDouble("offset-y", 0.2));
            aura.setOffsetZ(auraSection.getDouble("offset-z", 0.2));
            def.setAura(aura);
        }

        // Cooldown
        ConfigurationSection cdSection = cfg.getConfigurationSection("cooldown");
        if (cdSection != null) {
            def.setCooldownType(CooldownType.fromString(cdSection.getString("type")));
            def.setCooldownSeconds(cdSection.getLong("duration", 0));
            def.setCooldownSharedKey(cdSection.getString("shared-key", ""));
        }

        return def;
    }

    public boolean save(RelicDefinition def, File file) {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("id", def.getId());
        cfg.set("display-name", def.getDisplayName());
        cfg.set("lore", def.getLore());
        cfg.set("material", def.getMaterial().name());
        cfg.set("rarity", def.getRarity());
        if (def.getCustomModelData() > 0) cfg.set("custom-model-data", def.getCustomModelData());
        cfg.set("glow", def.isGlow());
        if (def.getSkullTexture() != null) cfg.set("skull-texture", def.getSkullTexture());
        if (def.getPermission() != null && !def.getPermission().isEmpty())
            cfg.set("permission", def.getPermission());
        cfg.set("category", def.getCategory());

        // Triggers
        Map<TriggerType, List<AbilityConfig>> trigMap = def.getTriggers();
        for (Map.Entry<TriggerType, List<AbilityConfig>> entry : trigMap.entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (AbilityConfig ac : entry.getValue()) {
                Map<String, Object> m = new LinkedHashMap<>(ac.getParams());
                m.put("type", ac.getType());
                list.add(m);
            }
            cfg.set("triggers." + entry.getKey().name(), list);
        }

        // Cooldown
        cfg.set("cooldown.type", def.getCooldownType().name());
        cfg.set("cooldown.duration", def.getCooldownSeconds());
        cfg.set("cooldown.shared-key", def.getCooldownSharedKey());

        try {
            cfg.save(file);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save relic " + def.getId(), e);
            return false;
        }
    }
}
