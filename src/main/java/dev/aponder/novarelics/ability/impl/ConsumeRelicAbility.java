package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.relic.RelicBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ConsumeRelicAbility extends Ability {

    public ConsumeRelicAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String relicId = context.getRelic().getId();
        int toConsume = config.getInt("amount", 1);

        for (int slot = 0; slot < player.getInventory().getSize() && toConsume > 0; slot++) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item == null) continue;
            if (!relicId.equals(RelicBuilder.getRelicId(item))) continue;

            if (item.getAmount() <= toConsume) {
                toConsume -= item.getAmount();
                player.getInventory().setItem(slot, null);
            } else {
                item.setAmount(item.getAmount() - toConsume);
                toConsume = 0;
            }
        }

        return true;
    }

    @Override
    public String getName() { return "Consume Relic"; }

    @Override
    public String getDescription() { return "Removes one or more of this relic from the player's inventory."; }
}
