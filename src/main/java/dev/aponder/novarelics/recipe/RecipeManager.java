package dev.aponder.novarelics.recipe;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

public class RecipeManager {

    private final NovaRelics plugin;
    private final List<NamespacedKey> registered = new ArrayList<>();

    public RecipeManager(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("recipes.enabled", true)) return;
        for (RelicDefinition relic : plugin.getRelicManager().getAllRelics()) {
            registerRelicRecipes(relic);
        }
    }

    public void unregister() {
        for (NamespacedKey key : registered) {
            Bukkit.removeRecipe(key);
        }
        registered.clear();
    }

    private void registerRelicRecipes(RelicDefinition relic) {
        // Recipes are stored in the relic's custom data for now
        // Future: parse recipe configs from relic YAML
        Object recipesObj = relic.getCustomData().get("recipes");
        if (!(recipesObj instanceof List<?> recipeList)) return;

        int index = 0;
        for (Object rawRecipe : recipeList) {
            if (!(rawRecipe instanceof java.util.Map<?, ?> recipeMap)) continue;
            try {
                registerSingle(relic, recipeMap, index++);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register recipe for " + relic.getId()
                        + ": " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void registerSingle(RelicDefinition relic, java.util.Map<?, ?> rawMap, int index) {
        java.util.Map<String, Object> recipeMap = (java.util.Map<String, Object>) rawMap;
        String typeStr = String.valueOf(recipeMap.get("type"));
        RecipeType type = RecipeType.fromString(typeStr);
        ItemStack result = plugin.getRelicManager().buildItem(relic);
        result.setAmount(((Number) recipeMap.getOrDefault("amount", 1)).intValue());

        NamespacedKey key = new NamespacedKey(plugin, relic.getId() + "_recipe_" + index);

        switch (type) {
            case SHAPED -> {
                ShapedRecipe shaped = new ShapedRecipe(key, result);
                List<String> shape = (List<String>) recipeMap.get("shape");
                if (shape == null || shape.isEmpty()) return;
                shaped.shape(shape.toArray(new String[0]));
                java.util.Map<String, String> ingredients =
                        (java.util.Map<String, String>) recipeMap.get("ingredients");
                if (ingredients != null) {
                    ingredients.forEach((k, v) -> {
                        Material mat = Material.matchMaterial(v);
                        if (mat != null) shaped.setIngredient(k.charAt(0), mat);
                    });
                }
                Bukkit.addRecipe(shaped);
                registered.add(key);
            }
            case SHAPELESS -> {
                ShapelessRecipe shapeless = new ShapelessRecipe(key, result);
                List<String> ingredients = (List<String>) recipeMap.get("ingredients");
                if (ingredients == null) return;
                for (String mat : ingredients) {
                    Material m = Material.matchMaterial(mat);
                    if (m != null) shapeless.addIngredient(m);
                }
                Bukkit.addRecipe(shapeless);
                registered.add(key);
            }
            case FURNACE -> {
                String inputStr = String.valueOf(recipeMap.get("input"));
                Material input = Material.matchMaterial(inputStr);
                if (input == null) return;
                float exp = Float.parseFloat(String.valueOf(recipeMap.getOrDefault("experience", "0.1")));
                int cookTime = Integer.parseInt(String.valueOf(recipeMap.getOrDefault("cook-time", "200")));
                FurnaceRecipe furnace = new FurnaceRecipe(key, result, input, exp, cookTime);
                Bukkit.addRecipe(furnace);
                registered.add(key);
            }
            default -> plugin.getLogger().info("Recipe type " + type + " registration pending for " + relic.getId());
        }
    }
}
