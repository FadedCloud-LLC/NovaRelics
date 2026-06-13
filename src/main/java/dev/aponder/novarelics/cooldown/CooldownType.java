package dev.aponder.novarelics.cooldown;

public enum CooldownType {
    PER_PLAYER,
    GLOBAL,
    SHARED;

    public static CooldownType fromString(String s) {
        if (s == null) return PER_PLAYER;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PER_PLAYER;
        }
    }
}
