package dev.aponder.novarelics.hook;

import dev.aponder.novarelics.NovaRelics;

public class PlaceholderAPIHook {

    private final NovaRelics plugin;
    private boolean hooked;

    public PlaceholderAPIHook(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public boolean register() {
        try {
            Class.forName("me.clip.placeholderapi.expansion.PlaceholderExpansion");
            if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                hooked = false;
                return false;
            }
            hooked = new NovaRelicsExpansion(plugin).register();
        } catch (ClassNotFoundException e) {
            hooked = false;
        }
        return hooked;
    }

    public boolean isHooked() { return hooked; }
}
