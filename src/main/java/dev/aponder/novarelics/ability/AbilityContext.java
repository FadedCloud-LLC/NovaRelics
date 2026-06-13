package dev.aponder.novarelics.ability;

import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AbilityContext {

    private final Player player;
    private final RelicDefinition relic;
    private final ItemStack relicItem;
    private final TriggerType triggerType;
    private final Event originalEvent;
    private final Map<String, Object> data;
    private boolean cancelled;

    public AbilityContext(Player player, RelicDefinition relic, ItemStack relicItem,
                          TriggerType triggerType, Event originalEvent) {
        this.player = player;
        this.relic = relic;
        this.relicItem = relicItem;
        this.triggerType = triggerType;
        this.originalEvent = originalEvent;
        this.data = new HashMap<>();
    }

    public Player getPlayer() { return player; }
    public RelicDefinition getRelic() { return relic; }
    public ItemStack getRelicItem() { return relicItem; }
    public TriggerType getTriggerType() { return triggerType; }
    public Event getOriginalEvent() { return originalEvent; }
    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    public void put(String key, Object value) { data.put(key, value); }

    public Object get(String key) { return data.get(key); }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {
        Object v = data.get(key);
        if (v == null) return def;
        try { return (T) v; }
        catch (ClassCastException e) { return def; }
    }

    public boolean has(String key) { return data.containsKey(key); }

    public Map<String, Object> getData() { return data; }

    public <T extends Event> T getEvent(Class<T> type) {
        if (type.isInstance(originalEvent)) return type.cast(originalEvent);
        return null;
    }
}
