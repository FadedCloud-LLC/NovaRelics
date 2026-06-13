package dev.aponder.novarelics.aura;

public enum AuraType {
    VANILLA_ENCHANT_GLOW,
    CUSTOM_PARTICLE_AURA,
    HELD_ITEM_AURA,
    GROUND_AURA,
    EQUIPPED_AURA,
    TRAIL_EFFECT,
    CIRCLE_EFFECT,
    SPIRAL_EFFECT,
    DOUBLE_SPIRAL_EFFECT,
    PULSE_EFFECT,
    TOTEM_EFFECT,
    BEACON_EFFECT,
    MAGIC_EFFECT,
    FIRE_EFFECT,
    ICE_EFFECT,
    NATURE_EFFECT,
    SHADOW_EFFECT,
    LIGHTNING_EFFECT,
    CELESTIAL_EFFECT,
    VOID_EFFECT,
    GROUND_RING,
    CUSTOM;

    public static AuraType fromString(String s) {
        if (s == null) return CUSTOM_PARTICLE_AURA;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOM_PARTICLE_AURA;
        }
    }
}
