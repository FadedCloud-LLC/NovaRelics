package dev.aponder.novarelics.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    private Economy economy;
    private boolean enabled;

    public boolean register() {
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("Vault") == null) {
            enabled = false;
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                org.bukkit.Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            enabled = false;
            return false;
        }
        economy = rsp.getProvider();
        enabled = economy != null;
        return enabled;
    }

    public boolean isEnabled() { return enabled; }

    public double getBalance(Player player) {
        if (!enabled) return 0;
        return economy.getBalance(player);
    }

    public boolean deposit(Player player, double amount) {
        if (!enabled) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean withdraw(Player player, double amount) {
        if (!enabled) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean has(Player player, double amount) {
        if (!enabled) return false;
        return economy.has(player, amount);
    }

    public String format(double amount) {
        if (!enabled) return "$" + amount;
        return economy.format(amount);
    }
}
