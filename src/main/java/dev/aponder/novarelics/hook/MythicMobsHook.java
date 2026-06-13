package dev.aponder.novarelics.hook;

import org.bukkit.entity.Entity;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public class MythicMobsHook {

    private boolean enabled;
    private Object mobManager;
    private Method isActiveMob;
    private Method getActiveMob;

    public boolean register() {
        try {
            Class<?> mythicBukkitClass = Class.forName("io.lumine.mythic.bukkit.MythicBukkit");
            enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("MythicMobs");
            if (enabled) {
                Method inst = mythicBukkitClass.getMethod("inst");
                Object mythicBukkit = inst.invoke(null);
                Method getMobManager = mythicBukkitClass.getMethod("getMobManager");
                mobManager = getMobManager.invoke(mythicBukkit);
                isActiveMob = mobManager.getClass().getMethod("isActiveMob", UUID.class);
                getActiveMob = mobManager.getClass().getMethod("getActiveMob", UUID.class);
            }
        } catch (ClassNotFoundException e) {
            enabled = false;
        } catch (Exception e) {
            enabled = false;
        }
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public boolean isMythicMob(Entity entity) {
        if (!enabled || mobManager == null) return false;
        try {
            return (boolean) isActiveMob.invoke(mobManager, entity.getUniqueId());
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public String getMythicMobId(Entity entity) {
        if (!enabled || mobManager == null) return null;
        try {
            Optional<?> mob = (Optional<?>) getActiveMob.invoke(mobManager, entity.getUniqueId());
            if (mob == null || !mob.isPresent()) return null;
            Object activeMob = mob.get();
            Method getType = activeMob.getClass().getMethod("getType");
            Object type = getType.invoke(activeMob);
            Method getInternalName = type.getClass().getMethod("getInternalName");
            return (String) getInternalName.invoke(type);
        } catch (Exception e) {
            return null;
        }
    }
}
