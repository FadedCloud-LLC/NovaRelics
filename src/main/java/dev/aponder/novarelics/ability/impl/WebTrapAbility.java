package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class WebTrapAbility extends Ability {

    public WebTrapAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double radius = config.getDouble("radius", 4.0);
        int durationSeconds = config.getInt("duration", 3);
        boolean includePlayer = config.getBoolean("include-player", false);

        List<Location> placed = new ArrayList<>();

        for (Entity e : player.getWorld().getNearbyEntities(
                player.getLocation(), radius, radius, radius,
                e -> e instanceof LivingEntity && (includePlayer || !e.equals(player)))) {

            Location feet = e.getLocation().getBlock().getLocation();
            if (feet.getBlock().getType().isAir()) {
                feet.getBlock().setType(Material.COBWEB);
                placed.add(feet);
            }
        }

        if (!placed.isEmpty()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                    placed.forEach(loc -> {
                        if (loc.getBlock().getType() == Material.COBWEB) {
                            loc.getBlock().setType(Material.AIR);
                        }
                    }),
                    durationSeconds * 20L);
        }

        return !placed.isEmpty();
    }

    @Override
    public String getName() { return "Web Trap"; }

    @Override
    public String getDescription() { return "Places cobwebs at nearby entities' feet, removed after a duration."; }
}
