package dev.aponder.novarelics.trigger.impl;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.trigger.TriggerType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ClickTrigger implements Listener {

    private final NovaRelics plugin;

    public ClickTrigger(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInteract(PlayerInteractEvent event) {
        // Ignore off-hand duplicate events
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();
        boolean sneaking = player.isSneaking();

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!plugin.getRelicManager().isRelic(item)) return;

        TriggerType type = switch (action) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK ->
                    sneaking ? TriggerType.SHIFT_RIGHT_CLICK : TriggerType.RIGHT_CLICK;
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK ->
                    sneaking ? TriggerType.SHIFT_LEFT_CLICK : TriggerType.LEFT_CLICK;
            default -> null;
        };

        if (type == null) return;

        plugin.getTriggerManager().dispatchFromHand(player, type, event);
    }
}
