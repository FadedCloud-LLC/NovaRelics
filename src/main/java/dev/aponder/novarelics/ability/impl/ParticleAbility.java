package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;

public class ParticleAbility extends Ability {

    public ParticleAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Particle particle = ParticleUtil.getParticle(config.getString("particle", "ENCHANT"));
        int count = config.getInt("count", 10);
        double ox = config.getDouble("offset-x", 0.3);
        double oy = config.getDouble("offset-y", 0.5);
        double oz = config.getDouble("offset-z", 0.3);
        double speed = config.getDouble("speed", 0.1);

        Location loc = context.getPlayer().getLocation().add(0, 1, 0);
        ParticleUtil.spawnForNearby(loc, particle, count, ox, oy, oz, speed,
                ParticleUtil.getRenderDistance());
        return true;
    }

    @Override
    public String getName() { return "Particle"; }

    @Override
    public String getDescription() { return "Spawns a configurable particle effect at the player."; }
}
