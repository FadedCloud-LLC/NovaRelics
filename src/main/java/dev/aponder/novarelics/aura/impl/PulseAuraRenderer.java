package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ColorUtil;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class PulseAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        Particle particle = ParticleUtil.getParticle(aura.getParticle());
        double baseRadius = aura.getRadius();
        // Pulsing radius: oscillates between baseRadius and baseRadius * 1.5
        double pulse = Math.sin(tick * aura.getSpeed() * TWO_PI);
        double radius = baseRadius + (baseRadius * 0.5 * pulse);
        int amount = Math.min(aura.getAmount(), 20);
        double renderDist = ParticleUtil.getRenderDistance();

        for (int i = 0; i < amount; i++) {
            double angle = TWO_PI * i / amount;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 1.0 + (0.3 * pulse);
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
