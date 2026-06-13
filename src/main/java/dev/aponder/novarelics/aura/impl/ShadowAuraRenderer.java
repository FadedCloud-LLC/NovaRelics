package dev.aponder.novarelics.aura.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.aura.AuraRenderer;
import dev.aponder.novarelics.util.ColorUtil;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ShadowAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;
    private static final Color SHADOW = Color.fromRGB(0x33, 0x00, 0x66);

    @Override
    public void render(Player player, Location origin, AuraDefinition aura, long tick, NovaRelics plugin) {
        int amount = Math.min(aura.getAmount(), 16);
        double radius = aura.getRadius();
        double speed = aura.getSpeed();
        double renderDist = ParticleUtil.getRenderDistance();
        double baseAngle = tick * speed * TWO_PI;

        for (int i = 0; i < amount; i++) {
            double angle = baseAngle + (TWO_PI * i / amount);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 1.0 + Math.sin(angle * 2) * 0.3;
            Location loc = origin.clone().add(x, y, z);
            ParticleUtil.spawnForNearby(loc, Particle.PORTAL, 1, 0, 0, 0, 0.1, renderDist);
        }
        // Dark dust on the ground
        Color color = aura.getColor() != null ? ColorUtil.toBukkitColor(aura.getColor()) : SHADOW;
        ParticleUtil.spawnDust(origin.clone().add(0, 0.05, 0), color, 1.5f, 3, renderDist);
    }
}
