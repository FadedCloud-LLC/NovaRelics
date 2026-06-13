package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ShockwaveAbility extends Ability {

    public ShockwaveAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double radius = config.getDouble("radius", 6.0);
        double damage = config.getDouble("damage", 4.0);
        boolean knockback = config.getBoolean("knockback", true);
        double knockPower = config.getDouble("knockback-power", 1.5);

        for (Entity e : player.getWorld().getNearbyEntities(
                player.getLocation(), radius, radius, radius,
                e -> e instanceof LivingEntity && !e.equals(player))) {

            ((LivingEntity) e).damage(damage, player);

            if (knockback) {
                Vector dir = e.getLocation().toVector()
                        .subtract(player.getLocation().toVector())
                        .normalize()
                        .multiply(knockPower)
                        .setY(0.5);
                e.setVelocity(dir);
            }
        }

        return true;
    }

    @Override
    public String getName() { return "Shockwave"; }

    @Override
    public String getDescription() { return "Deals damage and knocks back all nearby entities."; }
}
