package dev.aponder.novarelics.hook;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

public class HeadDatabaseHook {

    private boolean enabled;
    private Object api;
    private Method getItemHead;

    public boolean register() {
        try {
            Class<?> apiClass = Class.forName("me.arcaniax.hdb.api.HeadDatabaseAPI");
            enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("HeadDatabase");
            if (enabled) {
                api = apiClass.getDeclaredConstructor().newInstance();
                getItemHead = apiClass.getMethod("getItemHead", String.class);
            }
        } catch (ClassNotFoundException e) {
            enabled = false;
        } catch (Exception e) {
            enabled = false;
        }
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public ItemStack getHead(String id) {
        if (!enabled || id == null || id.isEmpty() || api == null) return null;
        try {
            return (ItemStack) getItemHead.invoke(api, id);
        } catch (Exception e) {
            return null;
        }
    }
}
