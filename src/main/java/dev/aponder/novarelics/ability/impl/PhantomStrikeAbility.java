package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PhantomStrikeAbility extends Ability {

    public PhantomStrikeAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        int distance = config.getInt("distance", 20);
        double offset = config.getDouble("offset", 1.5);

        Entity target = player.getTargetEntity(distance, false);
        if (!(target instanceof LivingEntity)) return false;

        // Position directly behind the target relative to its facing, offset blocks away
        Vector behind = target.getLocation().getDirection().normalize().multiply(-offset);
        Location dest = target.getLocation().clone().add(behind).add(0, 0.1, 0);
        dest.setYaw(target.getLocation().getYaw());
        dest.setPitch(0);

        player.teleport(dest);
        return true;
    }

    @Override
    public String getName() { return "Phantom Strike"; }

    @Override
    public String getDescription() { return "Teleports the player behind the entity they are looking at."; }
}
