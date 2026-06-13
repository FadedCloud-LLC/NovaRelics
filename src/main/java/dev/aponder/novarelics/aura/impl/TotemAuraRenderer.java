package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.Random;

public class TotemAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;
    private static final Random RNG = new Random();

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        int amount = Math.min(aura.getAmount(), 10);
        double radius = aura.getRadius();
        double speed = aura.getSpeed();
        double renderDist = ParticleUtil.getRenderDistance();
        double baseAngle = tick * speed * TWO_PI;

        for (int i = 0; i < amount; i++) {
            double angle = baseAngle + (TWO_PI * i / amount);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 0.5 + RNG.nextDouble() * 1.5;
            Location loc = origin.clone().add(x, y, z);
            ParticleUtil.spawnForNearby(loc, ParticleUtil.TOTEM_PARTICLE, 1, 0, 0, 0, 0, renderDist);
        }
    }
}
