package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class FlightAbility extends Ability {

    public FlightAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        int durationSeconds = config.getInt("duration", 10);

        player.setAllowFlight(true);
        player.setFlying(true);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            GameMode gm = player.getGameMode();
            if (gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }
        }, durationSeconds * 20L);

        return true;
    }

    @Override
    public String getName() { return "Flight"; }

    @Override
    public String getDescription() { return "Grants the player temporary creative flight."; }
}
