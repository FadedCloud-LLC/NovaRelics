package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Player;

public class GiveXpAbility extends Ability {

    public GiveXpAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        int amount = config.getInt("amount", 10);
        boolean levels = config.getBoolean("levels", false);
        if (levels) {
            player.giveExpLevels(amount);
        } else {
            player.giveExp(amount);
        }
        return true;
    }

    @Override
    public String getName() { return "Give XP"; }

    @Override
    public String getDescription() { return "Grants experience points or levels to the player."; }
}
