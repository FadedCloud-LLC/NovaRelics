package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.ItemUtil;
import org.bukkit.inventory.ItemStack;

public class RepairPercentAbility extends Ability {

    public RepairPercentAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        double percent = config.getDouble("percent", 25.0);
        ItemStack held = context.getPlayer().getInventory().getItemInMainHand();
        if (!ItemUtil.isDamageable(held)) return false;

        ItemStack repaired = ItemUtil.repairByPercent(held, percent);
        context.getPlayer().getInventory().setItemInMainHand(repaired);
        return true;
    }

    @Override
    public String getName() { return "Repair Percent"; }

    @Override
    public String getDescription() { return "Repairs the held item by a configurable percentage."; }
}
