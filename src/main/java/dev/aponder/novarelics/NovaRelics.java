package dev.aponder.novarelics;

import dev.aponder.novarelics.ability.AbilityRegistry;
import dev.aponder.novarelics.aura.AuraManager;
import dev.aponder.novarelics.command.NovaRelicsCommand;
import dev.aponder.novarelics.config.ConfigManager;
import dev.aponder.novarelics.cooldown.CooldownManager;
import dev.aponder.novarelics.gui.ChatInputManager;
import dev.aponder.novarelics.gui.GuiManager;
import dev.aponder.novarelics.hook.HookManager;
import dev.aponder.novarelics.rarity.RarityManager;
import dev.aponder.novarelics.recipe.RecipeManager;
import dev.aponder.novarelics.relic.RelicManager;
import dev.aponder.novarelics.storage.StorageManager;
import dev.aponder.novarelics.texture.TextureManager;
import dev.aponder.novarelics.trigger.TriggerManager;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class NovaRelics extends JavaPlugin {

    private static final Set<String> SUPPORTED_VERSIONS = Set.of(
            "1.18.2", "1.19.4", "1.20.6", "1.21.11", "26.1.2"
    );

    private static NovaRelics instance;

    private ConfigManager configManager;
    private StorageManager storageManager;
    private HookManager hookManager;
    private RarityManager rarityManager;
    private AbilityRegistry abilityRegistry;
    private CooldownManager cooldownManager;
    private TextureManager textureManager;
    private RelicManager relicManager;
    private TriggerManager triggerManager;
    private AuraManager auraManager;
    private GuiManager guiManager;
    private ChatInputManager chatInputManager;
    private RecipeManager recipeManager;

    private boolean isVersionSupported() {
        String bukkit = Bukkit.getBukkitVersion();
        for (String v : SUPPORTED_VERSIONS) {
            if (bukkit.startsWith(v + "-")) return true;
        }
        return false;
    }

    @Override
    public void onEnable() {
        instance = this;

        if (!isVersionSupported()) {
            String version = Bukkit.getBukkitVersion().split("-")[0];
            console("<gradient:#AA55FF:#55FFFF><bold>NovaRelics</bold></gradient> <red>Unsupported server version: " + version);
            console("<gradient:#AA55FF:#55FFFF><bold>NovaRelics</bold></gradient> <red>Supported: 1.18.2, 1.19.4, 1.20.6, 1.21.11, 26.1.2");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        long start = System.currentTimeMillis();

        // 1. Config
        configManager = new ConfigManager(this);
        configManager.reload();

        // 2. Storage
        storageManager = new StorageManager(this);
        storageManager.connect();

        // 3. Hooks (before item building so texture hooks are ready)
        hookManager = new HookManager(this);
        hookManager.register();

        // 4. Core systems
        rarityManager = new RarityManager(this);
        abilityRegistry = new AbilityRegistry(this);
        cooldownManager = new CooldownManager(this);
        textureManager = new TextureManager(this);

        // 5. Relics
        relicManager = new RelicManager(this);
        relicManager.load();

        // 6. Triggers (registers event listeners)
        triggerManager = new TriggerManager(this);
        triggerManager.register();

        // 7. Aura rendering
        auraManager = new AuraManager(this);
        auraManager.start();

        // 8. GUI
        guiManager = new GuiManager(this);
        chatInputManager = new ChatInputManager(this);

        // 9. Recipes
        recipeManager = new RecipeManager(this);
        recipeManager.register();

        // 10. Commands
        NovaRelicsCommand cmd = new NovaRelicsCommand(this);
        getCommand("novarelics").setExecutor(cmd);
        getCommand("novarelics").setTabCompleter(cmd);

        // 11. PAPI expansion
        hookManager.registerPlaceholders();

        long elapsed = System.currentTimeMillis() - start;
        console("<gradient:#AA55FF:#55FFFF><bold>NovaRelics</bold></gradient> <gray>v"
                + getDescription().getVersion() + " enabled in " + elapsed + "ms. ("
                + relicManager.getCount() + " relics loaded)");
    }

    @Override
    public void onDisable() {
        if (auraManager != null) auraManager.stop();
        if (recipeManager != null) recipeManager.unregister();
        if (storageManager != null) storageManager.disconnect();
        console("<gradient:#AA55FF:#55FFFF><bold>NovaRelics</bold></gradient> <gray>disabled.");
    }

    /**
     * Hot reload — reloads configs, rarities, relics, aura system, and recipes.
     * Trigger listeners are persistent and do not need re-registration.
     */
    public void reload() {
        configManager.reload();
        rarityManager.reload();
        relicManager.reload();
        recipeManager.unregister();
        recipeManager.register();
        auraManager.stop();
        auraManager.start();
    }

    private void console(String miniMessage) {
        Bukkit.getConsoleSender().sendMessage(MiniMsg.parse(miniMessage));
    }

    public static NovaRelics getInstance() { return instance; }

    public ConfigManager getConfigManager() { return configManager; }
    public StorageManager getStorageManager() { return storageManager; }
    public HookManager getHookManager() { return hookManager; }
    public RarityManager getRarityManager() { return rarityManager; }
    public AbilityRegistry getAbilityRegistry() { return abilityRegistry; }
    public CooldownManager getCooldownManager() { return cooldownManager; }
    public TextureManager getTextureManager() { return textureManager; }
    public RelicManager getRelicManager() { return relicManager; }
    public TriggerManager getTriggerManager() { return triggerManager; }
    public AuraManager getAuraManager() { return auraManager; }
    public GuiManager getGuiManager() { return guiManager; }
    public ChatInputManager getChatInputManager() { return chatInputManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
}
