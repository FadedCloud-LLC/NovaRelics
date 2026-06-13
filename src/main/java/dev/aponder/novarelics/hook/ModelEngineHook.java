package dev.aponder.novarelics.hook;

public class ModelEngineHook {

    private boolean enabled;

    public boolean register() {
        enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ModelEngine");
        return enabled;
    }

    public boolean isEnabled() { return enabled; }
}
