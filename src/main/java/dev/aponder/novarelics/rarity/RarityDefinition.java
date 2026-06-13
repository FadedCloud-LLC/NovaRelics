package dev.aponder.novarelics.rarity;

import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RarityDefinition {

    private final String id;
    private String displayName;
    private String color;
    private String prefix;
    private boolean glow;
    private boolean auraEnabled;
    private String sound;
    private List<Map<String, Object>> particles;

    public RarityDefinition(String id) {
        this.id = id;
        this.particles = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getColor() { return color; }
    public String getPrefix() { return prefix; }
    public boolean isGlow() { return glow; }
    public boolean isAuraEnabled() { return auraEnabled; }
    public String getSound() { return sound; }
    public List<Map<String, Object>> getParticles() { return particles; }

    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setColor(String color) { this.color = color; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setGlow(boolean glow) { this.glow = glow; }
    public void setAuraEnabled(boolean auraEnabled) { this.auraEnabled = auraEnabled; }
    public void setSound(String sound) { this.sound = sound; }
    public void setParticles(List<Map<String, Object>> particles) { this.particles = particles; }

    public Sound getBukkitSound() {
        if (sound == null) return null;
        try {
            return Sound.valueOf(sound.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
