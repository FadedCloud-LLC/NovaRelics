package dev.aponder.novarelics.ability.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.Ability;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

public class CustomRecipeUnlockAbility extends Ability {

    public CustomRecipeUnlockAbility(NovaRelics plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(AbilityConfig config, AbilityContext context) {
        Player player = context.getPlayer();
        String recipeId = config.getString("recipe-id", "");
        if (recipeId.isEmpty()) return false;

        try {
            String[] parts = recipeId.contains(":") ? recipeId.split(":", 2)
                    : new String[]{"novarelics", recipeId};
            NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
            player.discoverRecipe(key);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to unlock recipe '" + recipeId + "': " + e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String getName() { return "Custom Recipe Unlock"; }

    @Override
    public String getDescription() { return "Unlocks a custom recipe for the player."; }
}
