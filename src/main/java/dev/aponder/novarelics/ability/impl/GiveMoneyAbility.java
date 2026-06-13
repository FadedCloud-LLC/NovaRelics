package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;

public class GiveMoneyAbility extends Ability {

    public GiveMoneyAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        if (!plugin.getHookManager().getVaultHook().isEnabled()) return false;
        double amount = config.getDouble("amount", 100.0);
        plugin.getHookManager().getVaultHook().deposit(context.getPlayer(), amount);
        return true;
    }

    @Override
    public String getName() { return "Give Money"; }

    @Override
    public String getDescription() { return "Gives the player Vault currency."; }
}
