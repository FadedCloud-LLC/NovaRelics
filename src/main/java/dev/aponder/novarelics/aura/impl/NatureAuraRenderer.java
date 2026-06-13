package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

public class NatureAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;
    private static final Random RNG = new Random();

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        int amount = Math.min(aura.getAmount(), 12);
        double radius = aura.getRadius();
        double renderDist = ParticleUtil.getRenderDistance();
        double speed = aura.getSpeed();
        double baseAngle = tick * speed * TWO_PI;

        for (int i = 0; i < amount; i++) {
            double angle = baseAngle + (TWO_PI * i / amount);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = RNG.nextDouble() * 2.0;
            Location loc = origin.clone().add(x, y, z);
            // Composter particles represent nature
            ParticleUtil.spawnForNearby(loc, Particle.COMPOSTER, 1, 0, 0.1, 0, 0.02, renderDist);
        }
        // Leaves floating
        for (int i = 0; i < 3; i++) {
            double x = (RNG.nextDouble() - 0.5) * radius * 2;
            double z = (RNG.nextDouble() - 0.5) * radius * 2;
            ParticleUtil.spawnForNearby(origin.clone().add(x, 2.0, z),
                    Particle.FALLING_DUST,
                    1, 0, 0.1, 0, 0,
                    org.bukkit.Material.OAK_LEAVES.createBlockData(),
                    renderDist);
        }
    }
}
