package dev.aponder.novarelics.hook;

import dev.aponder.novarelics.NovaRelics;

public class HookManager {

    private final NovaRelics plugin;

    private final VaultHook vaultHook = new VaultHook();
    private final PlaceholderAPIHook papiHook;
    private final LuckPermsHook luckPermsHook = new LuckPermsHook();
    private final ItemsAdderHook itemsAdderHook = new ItemsAdderHook();
    private final OraxenHook oraxenHook = new OraxenHook();
    private final NexoHook nexoHook = new NexoHook();
    private final HeadDatabaseHook headDatabaseHook = new HeadDatabaseHook();
    private final ProtocolLibHook protocolLibHook = new ProtocolLibHook();
    private final MythicMobsHook mythicMobsHook = new MythicMobsHook();
    private final ModelEngineHook modelEngineHook = new ModelEngineHook();

    public HookManager(NovaRelics plugin) {
        this.plugin = plugin;
        this.papiHook = new PlaceholderAPIHook(plugin);
    }

    public void register() {
        log("Vault",         vaultHook.register());
        log("LuckPerms",     luckPermsHook.register());
        log("ItemsAdder",    itemsAdderHook.register());
        log("Oraxen",        oraxenHook.register());
        log("Nexo",          nexoHook.register());
        log("HeadDatabase",  headDatabaseHook.register());
        log("ProtocolLib",   protocolLibHook.register());
        log("MythicMobs",    mythicMobsHook.register());
        log("ModelEngine",   modelEngineHook.register());
    }

    public void registerPlaceholders() {
        if (papiHook.register()) {
            plugin.getLogger().info("Hooked into PlaceholderAPI.");
        }
    }

    private void log(String name, boolean enabled) {
        if (enabled) {
            plugin.getLogger().info("Hooked into " + name + ".");
        } else if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info(name + " not found, skipping.");
        }
    }

    public VaultHook getVaultHook() { return vaultHook; }
    public PlaceholderAPIHook getPapiHook() { return papiHook; }
    public LuckPermsHook getLuckPermsHook() { return luckPermsHook; }
    public ItemsAdderHook getItemsAdderHook() { return itemsAdderHook; }
    public OraxenHook getOraxenHook() { return oraxenHook; }
    public NexoHook getNexoHook() { return nexoHook; }
    public HeadDatabaseHook getHeadDatabaseHook() { return headDatabaseHook; }
    public ProtocolLibHook getProtocolLibHook() { return protocolLibHook; }
    public MythicMobsHook getMythicMobsHook() { return mythicMobsHook; }
    public ModelEngineHook getModelEngineHook() { return modelEngineHook; }
}
