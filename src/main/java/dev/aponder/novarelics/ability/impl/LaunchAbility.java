package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class LaunchAbility extends Ability {

    public LaunchAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double vertical = config.getDouble("vertical", 1.5);
        double horizontal = config.getDouble("horizontal", 0.0);

        Vector vel = player.getVelocity().setY(vertical);
        if (horizontal > 0) {
            Vector look = player.getLocation().getDirection().setY(0).normalize().multiply(horizontal);
            vel = vel.add(look);
        }

        player.setVelocity(vel);
        player.setFallDistance(0f);
        return true;
    }

    @Override
    public String getName() { return "Launch"; }

    @Override
    public String getDescription() { return "Launches the player upward and optionally forward."; }
}
