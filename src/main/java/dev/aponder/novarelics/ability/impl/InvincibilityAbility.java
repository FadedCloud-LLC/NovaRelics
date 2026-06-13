package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Player;

public class InvincibilityAbility extends Ability {

    public InvincibilityAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        int ticks = config.getInt("duration", 60);
        player.setNoDamageTicks(Math.max(0, ticks));
        return true;
    }

    @Override
    public String getName() { return "Invincibility"; }

    @Override
    public String getDescription() { return "Grants the player temporary invincibility frames."; }
}
