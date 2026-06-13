package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;

public class TakeMoneyAbility extends Ability {

    public TakeMoneyAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        if (!plugin.getHookManager().getVaultHook().isEnabled()) return false;
        double amount = config.getDouble("amount", 100.0);
        double balance = plugin.getHookManager().getVaultHook().getBalance(context.getPlayer());
        if (balance < amount) return false;
        plugin.getHookManager().getVaultHook().withdraw(context.getPlayer(), amount);
        return true;
    }

    @Override
    public String getName() { return "Take Money"; }

    @Override
    public String getDescription() { return "Withdraws Vault currency from the player."; }
}
