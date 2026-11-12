package dev.aponder.novarelics.relic;

import dev.aponder.novarelics.NovaRelics;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class RelicManager {

    private final NovaRelics plugin;
    private final Map<String, RelicDefinition> relics = new LinkedHashMap<>();
    private RelicLoader loader;
    private RelicBuilder builder;

    public RelicManager(NovaRelics plugin) {
        this.plugin = plugin;
        this.loader = new RelicLoader(plugin);
        this.builder = new RelicBuilder(plugin);
    }

    public void load() {
        relics.clear();
        List<RelicDefinition> loaded = loader.loadAll();
        for (RelicDefinition def : loaded) {
            relics.put(def.getId(), def);
        }
        plugin.getLogger().info("Loaded " + relics.size() + " relic(s).");
    }

    public void reload() {
        this.loader = new RelicLoader(plugin);
        this.builder = new RelicBuilder(plugin);
        load();
    }

    public RelicDefinition getRelic(String id) {
        return relics.get(id == null ? null : id.toLowerCase());
    }

    public RelicDefinition getRelicFromItem(ItemStack item) {
        String id = RelicBuilder.getRelicId(item);
        if (id == null) return null;
        return getRelic(id);
    }

    public boolean isRelic(ItemStack item) {
        return RelicBuilder.isRelic(item);
    }

    public ItemStack buildItem(String relicId) {
        RelicDefinition def = getRelic(relicId);
        if (def == null) return null;
        return builder.build(def);
    }

    public ItemStack buildItem(RelicDefinition def) {
        return builder.build(def);
    }

    public void register(RelicDefinition def) {
        relics.put(def.getId(), def);
    }

    public boolean unregister(String id) {
        return relics.remove(id) != null;
    }

    /** Saves to {@code relics/<id>.yml}; refuses ids that are not safe file names (NR-012). */
    public boolean save(RelicDefinition def) {
        File file = RelicIds.fileIn(plugin.getConfigManager().getRelicsFolder(), def.getId());
        if (file == null) {
            plugin.getLogger().warning("Refusing to save relic with invalid id '" + def.getId() + "'");
            return false;
        }
        return loader.save(def, file);
    }

    public boolean delete(String id) {
        if (!relics.containsKey(id)) return false;
        File file = RelicIds.fileIn(plugin.getConfigManager().getRelicsFolder(), id);
        relics.remove(id);
        return file == null || !file.exists() || file.delete();
    }

    /** Copies every field (including aura and enchantments) under {@code newId} and saves it. */
    public RelicDefinition duplicate(String sourceId, String newId) {
        RelicDefinition source = getRelic(sourceId);
        if (source == null || !RelicIds.isValid(newId)) return null;
        RelicDefinition copy = loader.copy(source, newId);
        register(copy);
        save(copy);
        return copy;
    }

    public Collection<RelicDefinition> getAllRelics() { return relics.values(); }

    public List<String> getRelicIds() { return new ArrayList<>(relics.keySet()); }

    public List<RelicDefinition> getRelicsByCategory(String category) {
        return relics.values().stream()
                .filter(r -> category.equalsIgnoreCase(r.getCategory()))
                .collect(Collectors.toList());
    }

    public List<RelicDefinition> getRelicsByRarity(String rarity) {
        return relics.values().stream()
                .filter(r -> rarity.equalsIgnoreCase(r.getRarity()))
                .collect(Collectors.toList());
    }

    public int getCount() { return relics.size(); }

    public RelicLoader getLoader() { return loader; }
    public RelicBuilder getBuilder() { return builder; }
}
