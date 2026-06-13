package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ChargeAbility extends Ability {

    public ChargeAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double power = config.getDouble("power", 2.0);
        double vertical = config.getDouble("vertical", 0.2);

        Vector direction = player.getLocation().getDirection().normalize().multiply(power).setY(vertical);
        player.setVelocity(direction);
        player.setFallDistance(0f);
        return true;
    }

    @Override
    public String getName() { return "Charge"; }

    @Override
    public String getDescription() { return "Dashes the player forward in the direction they are looking."; }
}
