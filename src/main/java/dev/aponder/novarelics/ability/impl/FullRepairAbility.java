package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.ItemUtil;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class FullRepairAbility extends Ability {

    public FullRepairAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean prepare(AbilityConfig config, AbilityContext context) {
        PrepareAnvilEvent event = context.getEvent(PrepareAnvilEvent.class);
        if (event == null) return false;

        AnvilInventory inv = event.getInventory();
        ItemStack target = inv.getItem(0);
        if (!ItemUtil.isDamageable(target)) return false;
        if (ItemUtil.isFullyRepaired(target)) return false;

        ItemStack result = ItemUtil.fullyRepair(target);
        event.setResult(result);
        event.getInventory().setRepairCost(config.getInt("xp-cost", 0));

        context.put("target_item", target);
        return true;
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        // Money cost check via Vault
        double moneyCost = config.getDouble("money-cost", 0.0);
        if (moneyCost > 0 && plugin.getHookManager().getVaultHook().isEnabled()) {
            double balance = plugin.getHookManager().getVaultHook()
                    .getBalance(context.getPlayer());
            if (balance < moneyCost) {
                String msg = plugin.getConfigManager().getMessage("ability-full-repair-no-money",
                        "<cost>", String.valueOf(moneyCost));
                MiniMsg.send(context.getPlayer(), MiniMsg.parse(msg));
                return false;
            }
            plugin.getHookManager().getVaultHook().withdraw(context.getPlayer(), moneyCost);
        }

        // Success message
        ItemStack target = context.get("target_item", context.getRelicItem());
        String itemName = target.hasItemMeta() && target.getItemMeta().hasDisplayName()
                ? target.getItemMeta().getDisplayName()
                : target.getType().name();
        String msg = plugin.getConfigManager().getMessage("ability-full-repair-success",
                "<item>", itemName);
        MiniMsg.send(context.getPlayer(), MiniMsg.parse(msg));
        return true;
    }

    @Override
    public String getName() { return "Full Repair"; }

    @Override
    public String getDescription() { return "Fully repairs a damaged item when used in an anvil."; }
}
