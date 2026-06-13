package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ColorUtil;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class CircleAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        Particle particle = ParticleUtil.getParticle(aura.getParticle());
        double radius = aura.getRadius();
        double speed = aura.getSpeed();
        int amount = Math.min(aura.getAmount(), 30);
        double renderDist = ParticleUtil.getRenderDistance();
        double baseAngle = tick * speed * TWO_PI;
        double y = 1.0; // Waist height

        for (int i = 0; i < amount; i++) {
            double angle = baseAngle + (TWO_PI * i / amount);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location loc = origin.clone().add(x, y, z);

            if (particle == ParticleUtil.DUST_PARTICLE && aura.getColor() != null) {
                ParticleUtil.spawnDust(loc, ColorUtil.toBukkitColor(aura.getColor()),
                        aura.getDustSize(), 1, renderDist);
            } else {
                ParticleUtil.spawnForNearby(loc, particle, 1, 0, 0, 0, 0, renderDist);
            }
        }
    }
}
