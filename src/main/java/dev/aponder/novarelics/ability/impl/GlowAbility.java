package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class GlowAbility extends Ability {

    public GlowAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double radius = config.getDouble("radius", 10.0);
        int durationSeconds = config.getInt("duration", 5);
        boolean includePlayer = config.getBoolean("include-player", false);

        List<Entity> targets = player.getWorld().getNearbyEntities(
                player.getLocation(), radius, radius, radius,
                e -> e instanceof LivingEntity && (includePlayer || !e.equals(player))
        ).stream().toList();

        targets.forEach(e -> e.setGlowing(true));

        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> targets.forEach(e -> { if (e.isValid()) e.setGlowing(false); }),
                durationSeconds * 20L);

        return true;
    }

    @Override
    public String getName() { return "Glow"; }

    @Override
    public String getDescription() { return "Makes nearby entities glow (visible through walls) temporarily."; }
}
