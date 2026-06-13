package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NearbyHealAbility extends Ability {

    public NearbyHealAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double radius = config.getDouble("radius", 8.0);
        double amount = config.getDouble("amount", 4.0);
        boolean includeSelf = config.getBoolean("include-self", true);

        for (Entity e : player.getWorld().getNearbyEntities(
                player.getLocation(), radius, radius, radius,
                e -> e instanceof Player && (includeSelf || !e.equals(player)))) {

            Player target = (Player) e;
            double maxHp = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            target.setHealth(Math.min(target.getHealth() + amount, maxHp));
        }

        return true;
    }

    @Override
    public String getName() { return "Nearby Heal"; }

    @Override
    public String getDescription() { return "Heals all nearby players within a radius."; }
}
