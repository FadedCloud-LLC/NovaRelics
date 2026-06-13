package dev.aponder.novarelics.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class LuckPermsHook {

    private LuckPerms api;
    private boolean enabled;

    public boolean register() {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            enabled = false;
            return false;
        }
        try {
            api = LuckPermsProvider.get();
            enabled = true;
        } catch (Exception e) {
            enabled = false;
        }
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public String getPrimaryGroup(Player player) {
        if (!enabled) return "default";
        try {
            User user = api.getUserManager().getUser(player.getUniqueId());
            return user != null ? user.getPrimaryGroup() : "default";
        } catch (Exception e) {
            return "default";
        }
    }

    public boolean hasGroup(Player player, String group) {
        if (!enabled) return false;
        try {
            User user = api.getUserManager().getUser(player.getUniqueId());
            if (user == null) return false;
            return user.getInheritedGroups(user.getQueryOptions())
                    .stream()
                    .anyMatch(g -> g.getName().equalsIgnoreCase(group));
        } catch (Exception e) {
            return false;
        }
    }

    public LuckPerms getApi() { return api; }
}
