package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;

public class TeleportAbility extends Ability {

    private static final Random RNG = new Random();

    public TeleportAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String mode = config.getString("mode", "RANDOM").toUpperCase();

        Location dest = switch (mode) {
            case "COORDINATES" -> resolveCoordinates(config, player.getWorld());
            case "BED_SPAWN"   -> resolveBedSpawn(player);
            default            -> resolveRandom(config, player);
        };

        if (dest == null) {
            MiniMsg.send(player, MiniMsg.parse(
                    plugin.getConfigManager().getMessage("ability-no-target")));
            return false;
        }

        player.teleport(dest);
        MiniMsg.send(player, MiniMsg.parse(
                plugin.getConfigManager().getMessage("ability-teleport-success")));
        return true;
    }

    private Location resolveCoordinates(AbilityConfig config, World world) {
        double x = config.getDouble("x", 0);
        double y = config.getDouble("y", 64);
        double z = config.getDouble("z", 0);
        return new Location(world, x, y, z);
    }

    private Location resolveBedSpawn(Player player) {
        Location bed = player.getBedSpawnLocation();
        return bed != null ? bed : player.getWorld().getSpawnLocation();
    }

    private Location resolveRandom(AbilityConfig config, Player player) {
        double radius = config.getDouble("radius", 50);
        double minRadius = config.getDouble("min-radius", 5);
        boolean safe = config.getBoolean("safe", true);
        World world = player.getWorld();
        Location origin = player.getLocation();

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = RNG.nextDouble() * 2 * Math.PI;
            double dist = minRadius + RNG.nextDouble() * (radius - minRadius);
            double x = origin.getX() + Math.cos(angle) * dist;
            double z = origin.getZ() + Math.sin(angle) * dist;
            int y = world.getHighestBlockYAt((int) x, (int) z);
            Location candidate = new Location(world, x, y + 1, z,
                    origin.getYaw(), origin.getPitch());
            if (!safe || isSafe(candidate)) return candidate;
        }
        return null;
    }

    private boolean isSafe(Location loc) {
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Block ground = loc.clone().add(0, -1, 0).getBlock();
        return feet.getType().isAir() && head.getType().isAir() && ground.getType().isSolid();
    }

    @Override
    public String getName() { return "Teleport"; }

    @Override
    public String getDescription() { return "Teleports the player to a random or configured location."; }
}
