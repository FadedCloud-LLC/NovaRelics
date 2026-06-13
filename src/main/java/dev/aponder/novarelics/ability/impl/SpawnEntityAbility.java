package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class SpawnEntityAbility extends Ability {

    public SpawnEntityAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        String typeName = config.getString("type", "ZOMBIE");
        int count = config.getInt("count", 1);

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown entity type: " + typeName);
            return false;
        }

        Location loc = context.getPlayer().getLocation();
        for (int i = 0; i < count; i++) {
            loc.getWorld().spawnEntity(loc, entityType);
        }
        return true;
    }

    @Override
    public String getName() { return "Spawn Entity"; }

    @Override
    public String getDescription() { return "Spawns one or more entities at the player's location."; }
}
