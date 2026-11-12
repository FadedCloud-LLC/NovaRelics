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
        // Respect other plugins disabling drops, and creative mode (no drops at all).
        if (!event.isDropItems()) return false;
        if (context.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE) return false;

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

        // NR-001: never duplicate a drop that is the block itself (ancient debris, silk-touched ores,
        // ore blocks...). Otherwise place -> break -> place repeats forever and duplicates items.
        Material self = block.getType();
        Collection<ItemStack> drops = block.getDrops(
                context.getPlayer().getInventory().getItemInMainHand());
        boolean dropped = false;
        for (ItemStack drop : drops) {
            if (drop == null || drop.getType().isAir() || drop.getType() == self) continue;
            block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
            dropped = true;
        }
        return dropped;
    }

    @Override
    public String getName() { return "Double Drops"; }

    @Override
    public String getDescription() { return "Doubles block drops when the relic is held."; }
}
