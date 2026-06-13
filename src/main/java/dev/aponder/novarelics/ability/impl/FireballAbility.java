package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FireballAbility extends Ability {

    public FireballAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        float yield = (float) config.getDouble("yield", 1.0f);
        boolean incendiary = config.getBoolean("fire", true);

        Vector direction = player.getLocation().getDirection().normalize().multiply(2);
        Fireball fireball = player.launchProjectile(Fireball.class, direction);
        fireball.setYield(yield);
        fireball.setIsIncendiary(incendiary);
        fireball.setShooter(player);
        return true;
    }

    @Override
    public String getName() { return "Fireball"; }

    @Override
    public String getDescription() { return "Launches a fireball in the player's facing direction."; }
}
