package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class CelestialAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        int amount = Math.min(aura.getAmount(), 20);
        double radius = aura.getRadius();
        double speed = aura.getSpeed();
        double renderDist = ParticleUtil.getRenderDistance();
        double baseAngle = tick * speed * TWO_PI;

        // Two orbiting rings at different heights
        for (int ring = 0; ring < 2; ring++) {
            double ringOffset = ring * Math.PI / 2;
            for (int i = 0; i < amount / 2; i++) {
                double angle = baseAngle + ringOffset + (TWO_PI * i / (amount / 2));
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                double y = 1.0 + ring * 0.8;
                Location loc = origin.clone().add(x, y, z);
                ParticleUtil.spawnForNearby(loc, Particle.END_ROD, 1, 0, 0, 0, 0, renderDist);
            }
        }
        // Star at the top
        ParticleUtil.spawnForNearby(origin.clone().add(0, 2.3, 0),
                ParticleUtil.TOTEM_PARTICLE, 2, 0.1, 0.1, 0.1, 0.05, renderDist);
    }
}
