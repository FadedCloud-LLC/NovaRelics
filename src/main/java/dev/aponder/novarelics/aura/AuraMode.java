package dev.aponder.novarelics.aura;

public enum AuraMode {
    AROUND_PLAYER,
    ABOVE_HEAD,
    HELD_ITEM,
    AROUND_ITEM,
    AROUND_ENTITY,
    AROUND_BLOCK,
    GROUND_RING,
    VERTICAL_SPIRAL,
    DOUBLE_SPIRAL,
    ORBITING_PARTICLES,
    ROTATING_CIRCLE,
    PULSING_SPHERE,
    CUSTOM_PATTERN;

    public static AuraMode fromString(String s) {
        if (s == null) return AROUND_PLAYER;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AROUND_PLAYER;
        }
    }
}
