package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AutoSmeltAbility extends Ability {

    public AutoSmeltAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        BlockBreakEvent event = context.getEvent(BlockBreakEvent.class);
        if (event == null) return false;

        Block block = event.getBlock();
        ItemStack tool = context.getPlayer().getInventory().getItemInMainHand();
        Collection<ItemStack> drops = block.getDrops(tool);

        List<ItemStack> smelted = new ArrayList<>();
        boolean converted = false;
        for (ItemStack drop : drops) {
            Material result = ItemUtil.getSmeltResult(drop.getType());
            if (result != null) {
                smelted.add(new ItemStack(result, drop.getAmount()));
                converted = true;
            } else {
                smelted.add(drop);
            }
        }

        if (!converted) return false;

        // Cancel normal drops and spawn smelted versions
        event.setDropItems(false);
        for (ItemStack drop : smelted) {
            block.getWorld().dropItemNaturally(block.getLocation(), drop);
        }
        return true;
    }

    @Override
    public String getName() { return "Auto Smelt"; }

    @Override
    public String getDescription() { return "Automatically smelts block drops when broken."; }
}
