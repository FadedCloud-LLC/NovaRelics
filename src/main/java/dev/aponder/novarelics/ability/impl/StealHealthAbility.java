package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.Optional;

public class StealHealthAbility extends Ability {

    public StealHealthAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double amount = config.getDouble("amount", 4.0);
        double radius = config.getDouble("radius", 5.0);

        Optional<Entity> nearest = player.getWorld()
                .getNearbyEntities(player.getLocation(), radius, radius, radius,
                        e -> e instanceof LivingEntity && !e.equals(player))
                .stream()
                .min(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));

        if (nearest.isEmpty()) return false;

        LivingEntity victim = (LivingEntity) nearest.get();
        double stolen = Math.min(amount, victim.getHealth());
        victim.damage(stolen, player);

        double maxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(player.getHealth() + stolen, maxHp));
        return true;
    }

    @Override
    public String getName() { return "Steal Health"; }

    @Override
    public String getDescription() { return "Drains health from the nearest entity and gives it to the player."; }
}
