package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;

public class KeepItemAbility extends Ability {

    public KeepItemAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        PlayerDeathEvent event = context.getEvent(PlayerDeathEvent.class);
        if (event == null) return false;

        // Remove this relic from the drops list so it returns to the player
        List<ItemStack> drops = event.getDrops();
        Iterator<ItemStack> it = drops.iterator();
        while (it.hasNext()) {
            ItemStack drop = it.next();
            if (drop != null && drop.equals(context.getRelicItem())) {
                it.remove();
                // Re-add to inventory on respawn via PlayerRespawnEvent listener in trigger
                context.getPlayer().getInventory().addItem(drop);
                break;
            }
        }
        return true;
    }

    @Override
    public String getName() { return "Keep Item"; }

    @Override
    public String getDescription() { return "Keeps the relic in the player's inventory on death."; }
}
