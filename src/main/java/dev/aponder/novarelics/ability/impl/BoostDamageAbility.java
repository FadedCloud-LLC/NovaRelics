package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BoostDamageAbility extends Ability {

    public BoostDamageAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        EntityDamageByEntityEvent event = context.getEvent(EntityDamageByEntityEvent.class);
        if (event == null) return false;

        double multiplier = config.getDouble("multiplier", 1.5);
        double flat = config.getDouble("amount", 0.0);

        double newDamage = (event.getDamage() * multiplier) + flat;
        event.setDamage(newDamage);
        return true;
    }

    @Override
    public String getName() { return "Boost Damage"; }

    @Override
    public String getDescription() { return "Multiplies or adds to the player's outgoing damage."; }
}
