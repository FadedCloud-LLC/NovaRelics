package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class LookTeleportAbility extends Ability {

    public LookTeleportAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        int distance = config.getInt("distance", 30);

        Block target = player.getTargetBlockExact(distance);
        if (target == null) return false;

        Location dest = target.getLocation().add(0.5, 1, 0.5);
        dest.setYaw(player.getLocation().getYaw());
        dest.setPitch(player.getLocation().getPitch());

        if (!dest.getBlock().getType().isAir()
                || !dest.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }

        player.teleport(dest);
        return true;
    }

    @Override
    public String getName() { return "Look Teleport"; }

    @Override
    public String getDescription() { return "Teleports the player to the top of the block they are looking at."; }
}
