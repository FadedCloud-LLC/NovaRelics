package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class GainHealthOnKillAbility extends Ability {

    public GainHealthOnKillAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double amount = config.getDouble("amount", 4.0);
        boolean percent = config.getBoolean("percent", false);
        boolean capAtMax = config.getBoolean("max-health-cap", true);

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healAmount = percent ? maxHealth * (amount / 100.0) : amount;
        double newHealth = player.getHealth() + healAmount;
        if (capAtMax) newHealth = Math.min(newHealth, maxHealth);
        player.setHealth(newHealth);
        return true;
    }

    @Override
    public String getName() { return "Gain Health On Kill"; }

    @Override
    public String getDescription() { return "Heals the player when they kill a mob."; }
}
