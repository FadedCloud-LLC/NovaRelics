package dev.aponder.novarelics.hook;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class NexoHook {

    private boolean enabled;
    private Class<?> nexoItemsClass;

    public boolean register() {
        try {
            nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
            enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Nexo");
        } catch (ClassNotFoundException e) {
            enabled = false;
        }
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public ItemStack buildItem(String id) {
        if (!enabled || id == null || id.isEmpty() || nexoItemsClass == null) return null;
        try {
            Method itemFromId = nexoItemsClass.getMethod("itemFromId", String.class);
            Object builder = itemFromId.invoke(null, id);
            if (builder == null) return null;
            Method build = builder.getClass().getMethod("build");
            return (ItemStack) build.invoke(builder);
        } catch (Exception e) {
            return null;
        }
    }
}
