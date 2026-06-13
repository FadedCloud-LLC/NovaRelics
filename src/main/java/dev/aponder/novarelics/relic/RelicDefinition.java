package dev.aponder.novarelics.relic;

import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.aura.AuraDefinition;
import dev.aponder.novarelics.cooldown.CooldownType;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class RelicDefinition {

    private String id;
    private String displayName;
    private List<String> lore;
    private Material material;
    private String rarity;
    private int customModelData;
    private boolean glow;
    private String skullTexture;
    private String headDatabaseId;
    private String itemsAdderId;
    private String oraxenId;
    private String nexoId;
    private Map<Enchantment, Integer> enchantments;
    private List<ItemFlag> itemFlags;
    private String permission;
    private String category;
    private int maxCharges;
    private Map<TriggerType, List<AbilityConfig>> triggers;
    private AuraDefinition aura;
    private long cooldownSeconds;
    private CooldownType cooldownType;
    private String cooldownSharedKey;
    private Map<String, Object> customData;

    public RelicDefinition(String id) {
        this.id = id;
        this.lore = new ArrayList<>();
        this.material = Material.PAPER;
        this.rarity = "common";
        this.enchantments = new LinkedHashMap<>();
        this.itemFlags = new ArrayList<>();
        this.triggers = new EnumMap<>(TriggerType.class);
        this.aura = new AuraDefinition();
        this.customData = new LinkedHashMap<>();
        this.maxCharges = -1;
        this.cooldownSeconds = 0;
        this.cooldownType = CooldownType.PER_PLAYER;
        this.cooldownSharedKey = "";
        this.permission = "";
        this.category = "general";
    }

    public boolean hasTrigger(TriggerType type) {
        return triggers.containsKey(type) && !triggers.get(type).isEmpty();
    }

    public List<AbilityConfig> getAbilitiesForTrigger(TriggerType type) {
        return triggers.getOrDefault(type, Collections.emptyList());
    }

    // Getters
    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public Material getMaterial() { return material; }
    public String getRarity() { return rarity; }
    public int getCustomModelData() { return customModelData; }
    public boolean isGlow() { return glow; }
    public String getSkullTexture() { return skullTexture; }
    public String getHeadDatabaseId() { return headDatabaseId; }
    public String getItemsAdderId() { return itemsAdderId; }
    public String getOraxenId() { return oraxenId; }
    public String getNexoId() { return nexoId; }
    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }
    public List<ItemFlag> getItemFlags() { return itemFlags; }
    public String getPermission() { return permission; }
    public String getCategory() { return category; }
    public int getMaxCharges() { return maxCharges; }
    public Map<TriggerType, List<AbilityConfig>> getTriggers() { return triggers; }
    public AuraDefinition getAura() { return aura; }
    public long getCooldownSeconds() { return cooldownSeconds; }
    public CooldownType getCooldownType() { return cooldownType; }
    public String getCooldownSharedKey() { return cooldownSharedKey; }
    public Map<String, Object> getCustomData() { return customData; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setLore(List<String> lore) { this.lore = lore; }
    public void setMaterial(Material material) { this.material = material; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }
    public void setGlow(boolean glow) { this.glow = glow; }
    public void setSkullTexture(String skullTexture) { this.skullTexture = skullTexture; }
    public void setHeadDatabaseId(String headDatabaseId) { this.headDatabaseId = headDatabaseId; }
    public void setItemsAdderId(String itemsAdderId) { this.itemsAdderId = itemsAdderId; }
    public void setOraxenId(String oraxenId) { this.oraxenId = oraxenId; }
    public void setNexoId(String nexoId) { this.nexoId = nexoId; }
    public void setEnchantments(Map<Enchantment, Integer> enchantments) { this.enchantments = enchantments; }
    public void setItemFlags(List<ItemFlag> itemFlags) { this.itemFlags = itemFlags; }
    public void setPermission(String permission) { this.permission = permission; }
    public void setCategory(String category) { this.category = category; }
    public void setMaxCharges(int maxCharges) { this.maxCharges = maxCharges; }
    public void setTriggers(Map<TriggerType, List<AbilityConfig>> triggers) { this.triggers = triggers; }
    public void setAura(AuraDefinition aura) { this.aura = aura; }
    public void setCooldownSeconds(long cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
    public void setCooldownType(CooldownType cooldownType) { this.cooldownType = cooldownType; }
    public void setCooldownSharedKey(String cooldownSharedKey) { this.cooldownSharedKey = cooldownSharedKey; }
    public void setCustomData(Map<String, Object> customData) { this.customData = customData; }
}
