package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class LightningAbility extends Ability {

    public LightningAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        boolean damage = config.getBoolean("damage", true);
        double radius = config.getDouble("radius", 0);
        String target = config.getString("target", "CURSOR").toUpperCase();

        Location strikeLocation;
        if ("CURSOR".equals(target)) {
            strikeLocation = player.getTargetBlockExact(10) != null
                    ? player.getTargetBlockExact(10).getLocation()
                    : player.getLocation();
        } else {
            strikeLocation = player.getLocation();
        }

        if (radius > 0) {
            Collection<Entity> nearby = player.getWorld().getNearbyEntities(
                    strikeLocation, radius, radius, radius,
                    e -> e instanceof LivingEntity && !e.equals(player));
            for (Entity e : nearby) {
                if (damage) {
                    player.getWorld().strikeLightning(e.getLocation());
                } else {
                    player.getWorld().strikeLightningEffect(e.getLocation());
                }
            }
        } else {
            if (damage) {
                player.getWorld().strikeLightning(strikeLocation);
            } else {
                player.getWorld().strikeLightningEffect(strikeLocation);
            }
        }
        return true;
    }

    @Override
    public String getName() { return "Lightning"; }

    @Override
    public String getDescription() { return "Strikes lightning at the target location."; }
}
