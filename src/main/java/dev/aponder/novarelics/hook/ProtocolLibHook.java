package dev.aponder.novarelics.hook;

public class ProtocolLibHook {

    private boolean enabled;

    public boolean register() {
        enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ProtocolLib");
        return enabled;
    }

    public boolean isEnabled() { return enabled; }
}
