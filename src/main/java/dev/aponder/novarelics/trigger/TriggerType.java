package dev.aponder.novarelics.trigger;

public enum TriggerType {
    RIGHT_CLICK,
    LEFT_CLICK,
    SHIFT_RIGHT_CLICK,
    SHIFT_LEFT_CLICK,
    ANVIL,
    SMITHING_TABLE,
    GRINDSTONE,
    CONSUME,
    BREAK_BLOCK,
    PLACE_BLOCK,
    KILL_MOB,
    KILL_PLAYER,
    ENTITY_DAMAGE,
    ENTITY_DAMAGED,
    PLAYER_DEATH,
    PLAYER_RESPAWN,
    PROJECTILE_HIT,
    MOVE,
    JUMP,
    SNEAK,
    FISH,
    EQUIP,
    UNEQUIP,
    JOIN,
    QUIT,
    WORLD_CHANGE,
    COMMAND,
    CUSTOM_EVENT;

    public static TriggerType fromString(String s) {
        if (s == null) return null;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
