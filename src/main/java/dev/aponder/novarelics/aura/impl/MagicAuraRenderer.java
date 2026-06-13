package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

public class MagicAuraRenderer implements AuraRenderer {

    private static final Random RNG = new Random();

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        int amount = Math.min(aura.getAmount(), 15);
        double radius = aura.getRadius();
        double renderDist = ParticleUtil.getRenderDistance();

        for (int i = 0; i < amount; i++) {
            double x = (RNG.nextDouble() - 0.5) * radius * 2;
            double y = RNG.nextDouble() * 2.0;
            double z = (RNG.nextDouble() - 0.5) * radius * 2;
            Location loc = origin.clone().add(x, y, z);
            ParticleUtil.spawnForNearby(loc, Particle.SPELL_WITCH, 1, 0, 0, 0, 0, renderDist);
        }
    }
}
