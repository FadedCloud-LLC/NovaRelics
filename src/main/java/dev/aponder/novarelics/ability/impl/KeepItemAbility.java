package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class KeepItemAbility extends Ability {

    public KeepItemAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        PlayerDeathEvent event = context.getEvent(PlayerDeathEvent.class);
        if (event == null) return false;
        if (event.getKeepInventory()) return true; // nothing is dropped or cleared

        // NR-007: take the relic out of the drops and hand it back on respawn. Putting it into
        // the inventory here would be wiped: the server clears it after this event.
        ItemStack relicItem = context.getRelicItem();
        Iterator<ItemStack> it = event.getDrops().iterator();
        while (it.hasNext()) {
            ItemStack drop = it.next();
            if (drop != null && drop.isSimilar(relicItem)) {
                it.remove();
                plugin.getKeptItems().keep(context.getPlayer(), drop);
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() { return "Keep Item"; }

    @Override
    public String getDescription() { return "Keeps the relic in the player's inventory on death."; }
}
