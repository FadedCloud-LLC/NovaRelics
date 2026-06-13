package dev.aponder.novarelics.util;

import dev.aponder.novarelics.NovaRelics;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class ParticleUtil {

    private ParticleUtil() {}

    // Particle names changed in 1.20.6; resolve whichever name is present at runtime.
    public static final Particle DUST_PARTICLE = resolveParticle("DUST", "REDSTONE");
    public static final Particle ENCHANT_PARTICLE = resolveParticle("ENCHANT", "ENCHANTMENT_TABLE");
    public static final Particle TOTEM_PARTICLE = resolveParticle("TOTEM_OF_UNDYING", "TOTEM");

    private static Particle resolveParticle(String... names) {
        for (String name : names) {
            try { return Particle.valueOf(name); } catch (IllegalArgumentException ignored) {}
        }
        return Particle.FLAME;
    }

    public static Particle getParticle(String name) {
        if (name == null) return ENCHANT_PARTICLE;
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ENCHANT_PARTICLE;
        }
    }

    public static void spawnForNearby(Location location, Particle particle,
                                       int count, double offsetX, double offsetY,
                                       double offsetZ, double speed,
                                       double renderDistance) {
        World world = location.getWorld();
        if (world == null) return;
        double distSq = renderDistance * renderDistance;
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(location) <= distSq) {
                p.spawnParticle(particle, location, count,
                        offsetX, offsetY, offsetZ, speed);
            }
        }
    }

    public static void spawnDust(Location location, org.bukkit.Color color,
                                  float size, int count, double renderDistance) {
        World world = location.getWorld();
        if (world == null) return;
        double distSq = renderDistance * renderDistance;
        Particle.DustOptions dust = new Particle.DustOptions(color, size);
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(location) <= distSq) {
                p.spawnParticle(DUST_PARTICLE, location, count, 0, 0, 0, 0, dust);
            }
        }
    }

    public static void spawnDustTransition(Location location,
                                            org.bukkit.Color from, org.bukkit.Color to,
                                            float size, int count, double renderDistance) {
        World world = location.getWorld();
        if (world == null) return;
        double distSq = renderDistance * renderDistance;
        try {
            Particle.DustTransition transition =
                    new Particle.DustTransition(from, to, size);
            for (Player p : world.getPlayers()) {
                if (p.getLocation().distanceSquared(location) <= distSq) {
                    p.spawnParticle(Particle.DUST_COLOR_TRANSITION,
                            location, count, 0, 0, 0, 0, transition);
                }
            }
        } catch (Exception ignored) {
            // Fallback for versions without DUST_COLOR_TRANSITION
            spawnDust(location, from, size, count, renderDistance);
        }
    }

    public static <T> void spawnForNearby(Location location, Particle particle,
                                           int count, double offsetX, double offsetY,
                                           double offsetZ, double speed,
                                           T data, double renderDistance) {
        World world = location.getWorld();
        if (world == null) return;
        double distSq = renderDistance * renderDistance;
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(location) <= distSq) {
                p.spawnParticle(particle, location, count,
                        offsetX, offsetY, offsetZ, speed, data);
            }
        }
    }

    public static boolean isChunkLoaded(Location location) {
        if (location.getWorld() == null) return false;
        return location.getWorld().isChunkLoaded(
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4);
    }

    public static double getRenderDistance() {
        return NovaRelics.getInstance().getConfigManager()
                .getMainConfig().getDouble("aura.render-distance", 32);
    }
}
