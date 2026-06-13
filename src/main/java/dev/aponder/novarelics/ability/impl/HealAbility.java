package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealAbility extends Ability {

    public HealAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        double amount = config.getDouble("amount", 4.0);
        boolean percent = config.getBoolean("percent", false);

        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healAmount = percent ? maxHealth * (amount / 100.0) : amount;
        double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
        player.setHealth(newHealth);

        String msg = plugin.getConfigManager().getMessage("ability-heal-success",
                "<amount>", String.format("%.1f", healAmount / 2.0));
        MiniMsg.send(player, MiniMsg.parse(msg));
        return true;
    }

    @Override
    public String getName() { return "Heal"; }

    @Override
    public String getDescription() { return "Heals the player by a flat or percentage amount."; }
}
