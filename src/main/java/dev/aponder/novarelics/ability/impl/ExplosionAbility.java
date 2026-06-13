package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class ExplosionAbility extends Ability {

    public ExplosionAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        float power = (float) config.getDouble("power", 2.0);
        boolean fire = config.getBoolean("fire", false);
        boolean breakBlocks = config.getBoolean("break-blocks", false);
        String target = config.getString("target", "PLAYER").toUpperCase();

        Location loc;
        if ("CURSOR".equals(target)) {
            Block b = player.getTargetBlockExact(20);
            loc = b != null ? b.getLocation() : player.getLocation();
        } else {
            loc = player.getLocation();
        }

        player.getWorld().createExplosion(loc, power, fire, breakBlocks);
        return true;
    }

    @Override
    public String getName() { return "Explosion"; }

    @Override
    public String getDescription() { return "Creates an explosion at the player or cursor location."; }
}
