package dev.aponder.novarelics.rarity;

import dev.aponder.novarelics.NovaRelics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RarityManager {

    private final NovaRelics plugin;
    private final Map<String, RarityDefinition> rarities = new LinkedHashMap<>();

    public RarityManager(NovaRelics plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        rarities.clear();
        FileConfiguration cfg = plugin.getConfigManager().getRaritiesConfig();
        ConfigurationSection section = cfg.getConfigurationSection("rarities");
        if (section == null) {
            loadDefaults();
            return;
        }

        for (String key : section.getKeys(false)) {
            ConfigurationSection rs = section.getConfigurationSection(key);
            if (rs == null) continue;
            RarityDefinition def = new RarityDefinition(key.toLowerCase());
            def.setDisplayName(rs.getString("display-name", key));
            def.setColor(rs.getString("color", "<white>"));
            def.setPrefix(rs.getString("prefix", ""));
            def.setGlow(rs.getBoolean("glow", false));
            def.setAuraEnabled(rs.getBoolean("aura-enabled", false));
            def.setSound(rs.getString("sound", null));

            List<Map<?, ?>> rawParticles = rs.getMapList("particles");
            List<Map<String, Object>> particles = rawParticles.stream()
                    .map(m -> {
                        Map<String, Object> typed = new LinkedHashMap<>();
                        m.forEach((k, v) -> typed.put(k.toString(), v));
                        return typed;
                    })
                    .toList();
            def.setParticles(particles);

            rarities.put(key.toLowerCase(), def);
        }

        if (rarities.isEmpty()) loadDefaults();
        plugin.getLogger().info("Loaded " + rarities.size() + " rarities.");
    }

    private void loadDefaults() {
        String[] names = {"common", "uncommon", "rare", "epic", "legendary", "mythic", "divine"};
        String[] colors = {
            "<gradient:#888888:#BBBBBB>",
            "<gradient:#00AA00:#55FF55>",
            "<gradient:#0088FF:#55FFFF>",
            "<gradient:#AA00AA:#FF55FF>",
            "<gradient:#FFAA00:#FFD700>",
            "<gradient:#FF0000:#FF8800>",
            "<gradient:#FFFFFF:#FFFF00>"
        };
        boolean[] glows = {false, false, true, true, true, true, true};

        for (int i = 0; i < names.length; i++) {
            RarityDefinition def = new RarityDefinition(names[i]);
            def.setDisplayName(capitalize(names[i]));
            def.setColor(colors[i]);
            def.setPrefix(colors[i] + "[" + capitalize(names[i]) + "]</gradient>");
            def.setGlow(glows[i]);
            def.setAuraEnabled(i >= 3);
            rarities.put(names[i], def);
        }
    }

    public RarityDefinition getRarity(String id) {
        if (id == null) return rarities.get("common");
        return rarities.getOrDefault(id.toLowerCase(), rarities.get("common"));
    }

    public Collection<RarityDefinition> getRarities() { return rarities.values(); }

    public boolean exists(String id) { return id != null && rarities.containsKey(id.toLowerCase()); }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
