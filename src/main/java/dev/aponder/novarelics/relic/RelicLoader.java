package dev.aponder.novarelics.relic;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraMode;
import dev.aponder.novarelics.aura.AuraType;
import dev.aponder.novarelics.cooldown.CooldownType;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
                if (def == null) continue;
                if (!RelicIds.isValid(def.getId())) {
                    plugin.getLogger().warning("Skipping " + file.getName() + ": invalid relic id '" + def.getId()
                            + "' (allowed: " + RelicIds.RULE + ")");
                    continue;
                }
                loaded.add(def);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING,
                        "Failed to load relic from " + file.getName(), e);
            }
        }
        return loaded;
    }

    public RelicDefinition load(File file) {
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String fallbackId = file.getName().replace(".yml", "").toLowerCase().replace(" ", "_");
        return parse(cfg, fallbackId);
    }

    /** Builds a definition from YAML; {@code fallbackId} is used when the file has no {@code id}. */
    public RelicDefinition parse(ConfigurationSection cfg, String fallbackId) {
        String id = cfg.getString("id");
        if (id == null || id.isBlank()) id = fallbackId;

        RelicDefinition def = new RelicDefinition(id.toLowerCase());

        def.setDisplayName(cfg.getString("display-name", id));
        def.setLore(new ArrayList<>(cfg.getStringList("lore")));

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
                Enchantment ench = resolveEnchantment(key);
                if (ench != null) {
                    enchMap.put(ench, enchSection.getInt(key, 1));
                } else {
                    plugin.getLogger().warning("Relic " + def.getId() + ": unknown enchantment '" + key + "'");
                }
            }
            def.setEnchantments(enchMap);
        }

        // Item flags
        List<ItemFlag> flags = new ArrayList<>();
        for (String fs : cfg.getStringList("item-flags")) {
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

                List<AbilityConfig> abilities = new ArrayList<>();
                for (Map<?, ?> rawMap : trigSection.getMapList(trigKey)) {
                    Map<String, Object> params = new LinkedHashMap<>();
                    rawMap.forEach((k, v) -> params.put(k.toString(), v));
                    Object type = params.remove("type");
                    if (type == null) continue;
                    abilities.add(new AbilityConfig(type.toString(), params));
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

    /**
     * Resolves an enchantment by namespaced key ("sharpness", "minecraft:sharpness") first,
     * then by legacy Bukkit name ("DAMAGE_ALL") for older configs.
     */
    @SuppressWarnings("deprecation")
    static Enchantment resolveEnchantment(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        NamespacedKey key = lower.contains(":") ? NamespacedKey.fromString(lower) : NamespacedKey.minecraft(lower);
        Enchantment ench = key == null ? null : Enchantment.getByKey(key);
        return ench != null ? ench : Enchantment.getByName(name.toUpperCase(Locale.ROOT));
    }

    /**
     * Writes every field of {@code def} into {@code cfg}. Keys the plugin does not manage
     * (and their comments) are left untouched, so editing a hand-written file is safe.
     */
    public void serialize(RelicDefinition def, YamlConfiguration cfg) {
        cfg.set("id", def.getId());
        cfg.set("display-name", def.getDisplayName());
        cfg.set("material", def.getMaterial().name());
        cfg.set("rarity", def.getRarity());
        cfg.set("permission", emptyToNull(def.getPermission()));
        cfg.set("category", def.getCategory());
        cfg.set("lore", new ArrayList<>(def.getLore()));
        cfg.set("glow", def.isGlow());
        cfg.set("custom-model-data", def.getCustomModelData() > 0 ? def.getCustomModelData() : null);
        cfg.set("max-charges", def.getMaxCharges() >= 0 ? def.getMaxCharges() : null);
        cfg.set("skull-texture", emptyToNull(def.getSkullTexture()));
        cfg.set("head-database-id", emptyToNull(def.getHeadDatabaseId()));
        cfg.set("itemsadder-id", emptyToNull(def.getItemsAdderId()));
        cfg.set("oraxen-id", emptyToNull(def.getOraxenId()));
        cfg.set("nexo-id", emptyToNull(def.getNexoId()));

        cfg.set("item-flags", def.getItemFlags().isEmpty() ? null
                : def.getItemFlags().stream().map(Enum::name).toList());

        cfg.set("enchantments", null);
        for (Map.Entry<Enchantment, Integer> e : def.getEnchantments().entrySet()) {
            cfg.set("enchantments." + e.getKey().getKey().getKey(), e.getValue());
        }

        AuraDefinition aura = def.getAura();
        cfg.set("aura.enabled", aura.isEnabled());
        cfg.set("aura.type", aura.getType().name());
        cfg.set("aura.mode", aura.getMode().name());
        cfg.set("aura.particle", aura.getParticle());
        cfg.set("aura.color", aura.getColor());
        cfg.set("aura.from-color", aura.getFromColor());
        cfg.set("aura.to-color", aura.getToColor());
        cfg.set("aura.size", (double) aura.getDustSize());
        cfg.set("aura.radius", aura.getRadius());
        cfg.set("aura.speed", aura.getSpeed());
        cfg.set("aura.amount", aura.getAmount());
        cfg.set("aura.update-ticks", aura.getUpdateTicks());
        cfg.set("aura.only-when-held", aura.isOnlyWhenHeld());
        cfg.set("aura.offset-x", aura.getOffsetX());
        cfg.set("aura.offset-y", aura.getOffsetY());
        cfg.set("aura.offset-z", aura.getOffsetZ());

        cfg.set("triggers", null);
        for (Map.Entry<TriggerType, List<AbilityConfig>> entry : def.getTriggers().entrySet()) {
            List<Map<String, Object>> list = new ArrayList<>();
            for (AbilityConfig ac : entry.getValue()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("type", ac.getType());
                m.putAll(ac.getParams());
                list.add(m);
            }
            cfg.set("triggers." + entry.getKey().name(), list);
        }

        cfg.set("cooldown.type", def.getCooldownType().name());
        cfg.set("cooldown.duration", def.getCooldownSeconds());
        cfg.set("cooldown.shared-key", def.getCooldownSharedKey());
    }

    /**
     * Saves {@code def} to {@code file}. If the file exists it is updated in place
     * (unmanaged keys and comments are preserved) instead of being rewritten from scratch (NR-008).
     */
    public boolean save(RelicDefinition def, File file) {
        YamlConfiguration cfg = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        serialize(def, cfg);
        try {
            File parent = file.getParentFile();
            if (parent != null) parent.mkdirs();
            cfg.save(file);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save relic " + def.getId(), e);
            return false;
        }
    }

    /** Deep copy of a definition under a new id (all fields, including aura). */
    public RelicDefinition copy(RelicDefinition source, String newId) {
        YamlConfiguration cfg = new YamlConfiguration();
        serialize(source, cfg);
        cfg.set("id", newId);
        return parse(cfg, newId);
    }

    private static String emptyToNull(String s) {
        return s == null || s.isEmpty() ? null : s;
    }
}
