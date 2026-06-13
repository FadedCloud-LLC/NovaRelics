package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ColorUtil;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SpiralAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;
    private static final double HEIGHT = 2.0;
    private static final double RISE_SPEED = 0.03;

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        Particle particle = ParticleUtil.getParticle(aura.getParticle());
        double radius = aura.getRadius();
        double speed = aura.getSpeed();
        int amount = Math.min(aura.getAmount(), 30);
        double renderDist = ParticleUtil.getRenderDistance();

        double baseAngle = tick * speed * TWO_PI;
        double heightOffset = (tick * RISE_SPEED) % HEIGHT;

        for (int i = 0; i < amount; i++) {
            double angle = baseAngle + (TWO_PI * i / amount);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = heightOffset;

            Location loc = origin.clone().add(x, y, z);

            if (particle == ParticleUtil.DUST_PARTICLE && aura.getColor() != null) {
                ParticleUtil.spawnDust(loc, ColorUtil.toBukkitColor(aura.getColor()),
                        aura.getDustSize(), 1, renderDist);
            } else if (particle != null && particle.name().contains("DUST_COLOR_TRANSITION")
                    && aura.getFromColor() != null && aura.getToColor() != null) {
                ParticleUtil.spawnDustTransition(loc,
                        ColorUtil.toBukkitColor(aura.getFromColor()),
                        ColorUtil.toBukkitColor(aura.getToColor()),
                        aura.getDustSize(), 1, renderDist);
            } else {
                ParticleUtil.spawnForNearby(loc, particle, 1, 0, 0, 0, 0, renderDist);
            }
        }
    }
}
