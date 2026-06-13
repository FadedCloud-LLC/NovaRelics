package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SetOnFireAbility extends Ability {

    public SetOnFireAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double radius = config.getDouble("radius", 5.0);
        int durationSeconds = config.getInt("duration", 5);
        boolean includePlayer = config.getBoolean("include-player", false);

        player.getWorld().getNearbyEntities(
                player.getLocation(), radius, radius, radius,
                e -> e instanceof LivingEntity && (includePlayer || !e.equals(player))
        ).forEach(e -> e.setFireTicks(durationSeconds * 20));

        return true;
    }

    @Override
    public String getName() { return "Set On Fire"; }

    @Override
    public String getDescription() { return "Sets nearby entities on fire."; }
}
