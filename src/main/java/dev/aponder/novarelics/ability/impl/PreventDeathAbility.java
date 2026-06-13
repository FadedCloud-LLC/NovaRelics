package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Random;

public class PreventDeathAbility extends Ability {

    private static final Random RNG = new Random();

    public PreventDeathAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        PlayerDeathEvent event = context.getEvent(PlayerDeathEvent.class);
        if (event == null) return false;

        double chance = config.getDouble("chance", 100.0);
        if (RNG.nextDouble() * 100 > chance) return false;

        double healTo = config.getDouble("heal-to", 4.0);
        Player player = context.getPlayer();

        // Cancel death and heal
        event.setCancelled(true);
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(Math.min(healTo, maxHealth));

        MiniMsg.send(player, MiniMsg.parse(
                plugin.getConfigManager().getMessage("ability-prevent-death")));
        return true;
    }

    @Override
    public String getName() { return "Prevent Death"; }

    @Override
    public String getDescription() { return "Prevents the player from dying with a configurable chance."; }
}
