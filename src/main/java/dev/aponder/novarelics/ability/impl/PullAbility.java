package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PullAbility extends Ability {

    public PullAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double radius = config.getDouble("radius", 8.0);
        double power = config.getDouble("power", 2.0);
        double vertical = config.getDouble("vertical", 0.2);
        boolean includePlayer = config.getBoolean("include-player", false);

        for (Entity e : player.getWorld().getNearbyEntities(
                player.getLocation(), radius, radius, radius,
                e -> e instanceof LivingEntity && (includePlayer || !e.equals(player)))) {

            Vector dir = player.getLocation().toVector()
                    .subtract(e.getLocation().toVector())
                    .normalize()
                    .multiply(power)
                    .setY(vertical);
            e.setVelocity(dir);
        }

        return true;
    }

    @Override
    public String getName() { return "Pull"; }

    @Override
    public String getDescription() { return "Pulls nearby entities toward the player."; }
}
