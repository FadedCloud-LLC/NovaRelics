package dev.aponder.novarelics.hook;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class ItemsAdderHook {

    private boolean enabled;
    private Class<?> customStackClass;

    public boolean register() {
        try {
            customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
        } catch (ClassNotFoundException e) {
            enabled = false;
        }
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public ItemStack buildItem(String id) {
        if (!enabled || id == null || id.isEmpty() || customStackClass == null) return null;
        try {
            Method getInstance = customStackClass.getMethod("getInstance", String.class);
            Object customStack = getInstance.invoke(null, id);
            if (customStack == null) return null;
            Method getItemStack = customStack.getClass().getMethod("getItemStack");
            return (ItemStack) getItemStack.invoke(customStack);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isCustomItem(ItemStack item) {
        if (!enabled || item == null || customStackClass == null) return false;
        try {
            Method byItemStack = customStackClass.getMethod("byItemStack", ItemStack.class);
            return byItemStack.invoke(null, item) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
