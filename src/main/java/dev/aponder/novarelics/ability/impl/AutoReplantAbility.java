package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;
import java.util.Map;

public class AutoReplantAbility extends Ability {

    private static final Map<Material, Material> CROP_SEEDS = Map.of(
            Material.WHEAT,       Material.WHEAT,
            Material.CARROTS,     Material.CARROTS,
            Material.POTATOES,    Material.POTATOES,
            Material.BEETROOTS,   Material.BEETROOTS,
            Material.NETHER_WART, Material.NETHER_WART
    );

    public AutoReplantAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(AbilityConfig config, AbilityContext context) {
        BlockBreakEvent event = context.getEvent(BlockBreakEvent.class);
        if (event == null) return false;

        Block block = event.getBlock();
        Material cropType = block.getType();

        // Only act on fully grown crops
        BlockData data = block.getBlockData();
        if (!(data instanceof Ageable ageable)) return false;
        if (ageable.getAge() < ageable.getMaximumAge()) return false;

        List<String> allowedCrops = (List<String>) config.getParams().get("crops");
        if (allowedCrops != null && !allowedCrops.isEmpty()) {
            boolean allowed = allowedCrops.stream()
                    .anyMatch(c -> c.equalsIgnoreCase(cropType.name()));
            if (!allowed) return false;
        }

        if (!CROP_SEEDS.containsKey(cropType)) return false;

        int delayTicks = config.getInt("replant-delay-ticks", 1);
        final Block finalBlock = block;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (finalBlock.getType().isAir()) {
                finalBlock.setType(cropType);
                BlockData newData = finalBlock.getBlockData();
                if (newData instanceof Ageable newAgeable) {
                    newAgeable.setAge(0);
                    finalBlock.setBlockData(newAgeable);
                }
            }
        }, delayTicks);

        return true;
    }

    @Override
    public String getName() { return "Auto Replant"; }

    @Override
    public String getDescription() { return "Automatically replants crops after harvesting."; }
}
