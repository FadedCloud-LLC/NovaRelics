package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveItemAbility extends Ability {

    public GiveItemAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String materialName = config.getString("material", "STONE").toUpperCase();
        int amount = Math.max(1, Math.min(config.getInt("amount", 1), 64));

        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("GiveItemAbility: unknown material '" + materialName + "'");
            return false;
        }

        ItemStack item = new ItemStack(material, amount);
        player.getInventory().addItem(item).forEach((slot, leftover) ->
                player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        return true;
    }

    @Override
    public String getName() { return "Give Item"; }

    @Override
    public String getDescription() { return "Gives the player a specified item, dropping overflow naturally."; }
}
