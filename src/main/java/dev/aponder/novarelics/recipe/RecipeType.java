package dev.aponder.novarelics.recipe;

public enum RecipeType {
    SHAPED,
    SHAPELESS,
    ANVIL,
    SMITHING,
    STONECUTTER,
    FURNACE,
    BLAST_FURNACE,
    CAMPFIRE;

    public static RecipeType fromString(String s) {
        if (s == null) return SHAPED;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SHAPED;
        }
    }
}
