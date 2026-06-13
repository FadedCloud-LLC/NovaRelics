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

public class IceAuraRenderer implements AuraRenderer {

    private static final double TWO_PI = 2 * Math.PI;
    private static final Color ICE_BLUE = Color.fromRGB(0x55, 0xFF, 0xFF);

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
            double y = 1.0;
            Location loc = origin.clone().add(x, y, z);

            Color color = aura.getColor() != null
                    ? ColorUtil.toBukkitColor(aura.getColor())
                    : ICE_BLUE;
            ParticleUtil.spawnDust(loc, color, aura.getDustSize(), 1, renderDist);
        }
        // Snow particles at feet
        ParticleUtil.spawnForNearby(origin.clone().add(0, 0.1, 0),
                Particle.SNOWFLAKE, 2, radius * 0.5, 0.05, radius * 0.5, 0, renderDist);
    }
}
