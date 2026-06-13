package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.event.entity.EntityDamageEvent;

public class ReduceDamageAbility extends Ability {

    public ReduceDamageAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        EntityDamageEvent event = context.getEvent(EntityDamageEvent.class);
        if (event == null) return false;

        double multiplier = config.getDouble("multiplier", 0.5);
        double flat = config.getDouble("amount", 0.0);

        double reduced = Math.max(0, (event.getDamage() * multiplier) - flat);
        event.setDamage(reduced);
        return true;
    }

    @Override
    public String getName() { return "Reduce Damage"; }

    @Override
    public String getDescription() { return "Reduces incoming damage by a multiplier or flat amount."; }
}
