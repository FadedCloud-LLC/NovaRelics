package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.entity.Player;

public class FeedAbility extends Ability {

    public FeedAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        int hunger = config.getInt("hunger", 20);
        float saturation = (float) config.getDouble("saturation", 5.0);

        player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
        player.setSaturation(Math.min(20.0f, player.getSaturation() + saturation));

        MiniMsg.send(player, MiniMsg.parse(
                plugin.getConfigManager().getMessage("ability-feed-success")));
        return true;
    }

    @Override
    public String getName() { return "Feed"; }

    @Override
    public String getDescription() { return "Restores the player's hunger and saturation."; }
}
