package dev.aponder.novarelics.config;

import dev.aponder.novarelics.NovaRelics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final NovaRelics plugin;
    private FileConfiguration mainConfig;
    private FileConfiguration langConfig;
    private FileConfiguration raritiesConfig;
    private FileConfiguration englishFallback;

    public ConfigManager(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        mainConfig = plugin.getConfig();

        String lang = mainConfig.getString("language", "en").toLowerCase().trim();
        langConfig = loadLanguage(lang);
        raritiesConfig = loadOrSave("rarities.yml");

        ensureRelicsFolder();
    }

    private FileConfiguration loadLanguage(String lang) {
        // Always ensure en.yml is on disk as the ultimate fallback
        saveLanguageFile("en");

        // Save the requested language from the jar if available and not yet on disk
        if (!lang.equals("en")) {
            saveLanguageFile(lang);
        }

        File langFile = new File(plugin.getDataFolder(), "language/" + lang + ".yml");
        if (!langFile.exists()) {
            plugin.getLogger().warning("Language file 'language/" + lang + ".yml' not found — falling back to 'en'.");
            lang = "en";
            langFile = new File(plugin.getDataFolder(), "language/en.yml");
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(langFile);

        // Merge jar version of this language as defaults (fills in any keys the admin deleted)
        InputStream jarStream = plugin.getResource("language/" + lang + ".yml");
        if (jarStream != null) {
            YamlConfiguration jarDefaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(jarStream, StandardCharsets.UTF_8));
            cfg.setDefaults(jarDefaults);
        }

        // English fallback for keys missing from both the active file and its jar defaults (NR-036)
        englishFallback = loadEnglishFallback();

        plugin.getLogger().info("Language loaded: " + lang);
        return cfg;
    }

    private FileConfiguration loadEnglishFallback() {
        File enFile = new File(plugin.getDataFolder(), "language/en.yml");
        YamlConfiguration en = enFile.exists() ? YamlConfiguration.loadConfiguration(enFile) : new YamlConfiguration();
        InputStream jarEn = plugin.getResource("language/en.yml");
        if (jarEn != null) {
            en.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(jarEn, StandardCharsets.UTF_8)));
        }
        return en;
    }

    /**
     * Looks a message up in: active language file → that language's jar defaults →
     * English file → English jar defaults. Returns null if no source has it.
     * (getString(key, def) is not used on purpose: an explicit default bypasses setDefaults.)
     */
    private String lookup(String key) {
        String msg = langConfig.getString(key);
        if (msg == null && englishFallback != null) msg = englishFallback.getString(key);
        return msg;
    }

    private void saveLanguageFile(String lang) {
        File file = new File(plugin.getDataFolder(), "language/" + lang + ".yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                plugin.saveResource("language/" + lang + ".yml", false);
            } catch (IllegalArgumentException ignored) {
                // Not bundled in the jar — admin-created language, skip
            }
        }
    }

    private FileConfiguration loadOrSave(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        InputStream defStream = plugin.getResource(name);
        if (defStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            cfg.setDefaults(defaults);
        }
        return cfg;
    }

    private void ensureRelicsFolder() {
        File relicsDir = new File(plugin.getDataFolder(), "relics");
        if (!relicsDir.exists()) {
            relicsDir.mkdirs();
            String[] defaults = {
                    "relics/mending_wool.yml",
                    "relics/teleport_crystal.yml",
                    "relics/vampire_fang.yml",
                    "relics/ore_charm.yml",
                    "relics/harvest_charm.yml"
            };
            for (String path : defaults) {
                File f = new File(plugin.getDataFolder(), path);
                if (!f.exists() && plugin.getResource(path) != null) {
                    plugin.saveResource(path, false);
                }
            }
        }
    }

    public void save(String name, FileConfiguration cfg) {
        File file = new File(plugin.getDataFolder(), name);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + name + ": " + e.getMessage());
        }
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getRaritiesConfig() {
        return raritiesConfig;
    }

    public File getRelicsFolder() {
        return new File(plugin.getDataFolder(), "relics");
    }

    public String getMessage(String key) {
        String prefix = lookup("prefix");
        if (prefix == null) {
            prefix = "<gradient:#AA55FF:#55FFFF><bold>NovaRelics</bold></gradient> <dark_gray>»</dark_gray>";
        }
        String msg = lookup(key);
        if (msg == null) msg = key;
        return msg.replace("<prefix>", prefix);
    }

    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        return msg;
    }

    public boolean isDebug() {
        return mainConfig.getBoolean("debug", false);
    }
}
