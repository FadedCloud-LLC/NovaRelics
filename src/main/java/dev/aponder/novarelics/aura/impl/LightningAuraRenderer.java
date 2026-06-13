package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

public class LightningAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;
    private static final Random RNG = new Random();

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        int amount = Math.min(aura.getAmount(), 8);
        double radius = aura.getRadius();
        double renderDist = ParticleUtil.getRenderDistance();
        double speed = aura.getSpeed();
        double baseAngle = tick * speed * TWO_PI;

        // Electric sparks in a ring with jitter
        for (int i = 0; i < amount; i++) {
            double angle = baseAngle + (TWO_PI * i / amount);
            double jitter = (RNG.nextDouble() - 0.5) * 0.3;
            double x = Math.cos(angle) * (radius + jitter);
            double z = Math.sin(angle) * (radius + jitter);
            double y = 1.0 + (RNG.nextDouble() - 0.5) * 0.5;
            Location loc = origin.clone().add(x, y, z);
            try {
                ParticleUtil.spawnForNearby(loc, Particle.ELECTRIC_SPARK, 1, 0, 0, 0, 0, renderDist);
            } catch (Exception e) {
                ParticleUtil.spawnForNearby(loc, Particle.CRIT_MAGIC, 1, 0, 0, 0, 0.1, renderDist);
            }
        }
    }
}
