package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.MiniMsg;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;

public class DoubleDropsAbility extends Ability {

    public DoubleDropsAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(AbilityConfig config, AbilityContext context) {
        BlockBreakEvent event = context.getEvent(BlockBreakEvent.class);
        if (event == null) return false;

        Block block = event.getBlock();
        List<String> allowedBlocks = (List<String>) config.getParams().get("blocks");

        if (allowedBlocks != null && !allowedBlocks.isEmpty()) {
            boolean matches = allowedBlocks.stream()
                    .anyMatch(b -> b.equalsIgnoreCase(block.getType().name()));
            if (!matches) return false;
        }

        boolean requiresSilkTouch = config.getBoolean("requires-silk-touch", false);
        if (requiresSilkTouch) {
            ItemStack tool = context.getPlayer().getInventory().getItemInMainHand();
            if (tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.SILK_TOUCH) == 0) {
                return false;
            }
        }

        Collection<ItemStack> drops = block.getDrops(
                context.getPlayer().getInventory().getItemInMainHand());
        for (ItemStack drop : drops) {
            if (drop != null && !drop.getType().isAir()) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
            }
        }
        return true;
    }

    @Override
    public String getName() { return "Double Drops"; }

    @Override
    public String getDescription() { return "Doubles block drops when the relic is held."; }
}
