package dev.aponder.novarelics.hook;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class OraxenHook {

    private boolean enabled;
    private Class<?> oraxenItemsClass;

    public boolean register() {
        try {
            oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Oraxen");
        } catch (ClassNotFoundException e) {
            enabled = false;
        }
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public ItemStack buildItem(String id) {
        if (!enabled || id == null || id.isEmpty() || oraxenItemsClass == null) return null;
        try {
            Method getById = oraxenItemsClass.getMethod("getItemById", String.class);
            Object builder = getById.invoke(null, id);
            if (builder == null) return null;
            Method build = builder.getClass().getMethod("build");
            return (ItemStack) build.invoke(builder);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isCustomItem(ItemStack item) {
        if (!enabled || item == null || oraxenItemsClass == null) return false;
        try {
            Method getIdByItem = oraxenItemsClass.getMethod("getIdByItem", ItemStack.class);
            return getIdByItem.invoke(null, item) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
