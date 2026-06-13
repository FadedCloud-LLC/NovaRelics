package dev.aponder.novarelics.trigger;

import dev.aponder.novarelics.relic.RelicDefinition;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TriggerContext {

    private final Player player;
    private final RelicDefinition relic;
    private final ItemStack relicItem;
    private final TriggerType type;
    private final Event event;
    private final Map<String, Object> data;

    public TriggerContext(Player player, RelicDefinition relic, ItemStack relicItem,
                          TriggerType type, Event event) {
        this.player = player;
        this.relic = relic;
        this.relicItem = relicItem;
        this.type = type;
        this.event = event;
        this.data = new HashMap<>();
    }

    public Player getPlayer() { return player; }
    public RelicDefinition getRelic() { return relic; }
    public ItemStack getRelicItem() { return relicItem; }
    public TriggerType getType() { return type; }
    public Event getEvent() { return event; }
    public Map<String, Object> getData() { return data; }
    public void put(String key, Object value) { data.put(key, value); }
    public Object get(String key) { return data.get(key); }
}
