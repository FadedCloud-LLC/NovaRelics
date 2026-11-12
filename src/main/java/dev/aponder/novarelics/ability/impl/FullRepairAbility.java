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
        // NR-005: payment happens first; any failure cancels the anvil click so the
        // repaired result cannot be taken for free.
        double moneyCost = config.getDouble("money-cost", 0.0);
        if (moneyCost > 0) {
            var vault = plugin.getHookManager().getVaultHook();
            if (!vault.isEnabled()) {
                plugin.getLogger().warning("FULL_REPAIR on relic " + context.getRelic().getId()
                        + " has money-cost " + moneyCost + " but no Vault economy is available; denying.");
                return deny(context);
            }
            if (vault.getBalance(context.getPlayer()) < moneyCost
                    || !vault.withdraw(context.getPlayer(), moneyCost)) {
                String msg = plugin.getConfigManager().getMessage("ability-full-repair-no-money",
                        "<cost>", String.valueOf(moneyCost));
                MiniMsg.send(context.getPlayer(), MiniMsg.parse(msg));
                return deny(context);
            }
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

    private static boolean deny(AbilityContext context) {
        if (context.getOriginalEvent() instanceof org.bukkit.event.Cancellable click) click.setCancelled(true);
        return false;
    }

    @Override
    public String getName() { return "Full Repair"; }

    @Override
    public String getDescription() { return "Fully repairs a damaged item when used in an anvil."; }
}
