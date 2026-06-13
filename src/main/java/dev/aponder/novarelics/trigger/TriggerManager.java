package dev.aponder.novarelics.trigger;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.ability.AbilityConfig;
import dev.aponder.novarelics.ability.AbilityContext;
import dev.aponder.novarelics.cooldown.CooldownManager;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.relic.RelicManager;
import dev.aponder.novarelics.trigger.impl.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class TriggerManager {

    private final NovaRelics plugin;

    public TriggerManager(NovaRelics plugin) {
        this.plugin = plugin;
    }

    public void register() {
        Listener[] listeners = {
            new ClickTrigger(plugin),
            new AnvilTrigger(plugin),
            new SmithingTableTrigger(plugin),
            new GrindstoneTrigger(plugin),
            new ConsumeTrigger(plugin),
            new BreakBlockTrigger(plugin),
            new PlaceBlockTrigger(plugin),
            new KillMobTrigger(plugin),
            new KillPlayerTrigger(plugin),
            new EntityDamageTrigger(plugin),
            new EntityDamagedTrigger(plugin),
            new PlayerDeathTrigger(plugin),
            new PlayerRespawnTrigger(plugin),
            new ProjectileHitTrigger(plugin),
            new MoveTrigger(plugin),
            new JumpTrigger(plugin),
            new SneakTrigger(plugin),
            new FishTrigger(plugin),
            new EquipTrigger(plugin),
            new JoinQuitTrigger(plugin),
            new WorldChangeTrigger(plugin),
            new CommandTrigger(plugin)
        };

        for (Listener l : listeners) {
            plugin.getServer().getPluginManager().registerEvents(l, plugin);
        }
    }

    /**
     * Central ability dispatch — called by every trigger implementation.
     * Handles cooldown checks, permission checks, and fires each ability config.
     */
    public void dispatch(Player player, ItemStack relicItem, RelicDefinition relic,
                         TriggerType triggerType, Event event) {
        if (!relic.hasTrigger(triggerType)) return;

        // Permission check
        String perm = relic.getPermission();
        if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) return;

        // Cooldown check
        CooldownManager cooldowns = plugin.getCooldownManager();
        if (cooldowns.isOnCooldown(player, relic)) {
            cooldowns.notifyPlayer(player, relic);
            return;
        }

        // Build ability context
        AbilityContext ctx = new AbilityContext(player, relic, relicItem, triggerType, event);

        // Fire all abilities for this trigger
        List<AbilityConfig> abilities = relic.getAbilitiesForTrigger(triggerType);
        boolean anySucceeded = false;
        for (AbilityConfig abilityConfig : abilities) {
            boolean ok = plugin.getAbilityRegistry().fire(abilityConfig, ctx);
            if (ok) anySucceeded = true;
        }

        // Apply cooldown only if at least one ability ran successfully
        if (anySucceeded && relic.getCooldownSeconds() > 0) {
            cooldowns.applyCooldown(player, relic);
        }
    }

    /**
     * Two-phase prepare dispatch for ANVIL and SMITHING_TABLE triggers.
     */
    public boolean dispatchPrepare(Player player, ItemStack relicItem, RelicDefinition relic,
                                    TriggerType triggerType, Event event) {
        if (!relic.hasTrigger(triggerType)) return false;
        AbilityContext ctx = new AbilityContext(player, relic, relicItem, triggerType, event);

        List<AbilityConfig> abilities = relic.getAbilitiesForTrigger(triggerType);
        boolean anySucceeded = false;
        for (AbilityConfig abilityConfig : abilities) {
            if (plugin.getAbilityRegistry().firePrepare(abilityConfig, ctx)) {
                anySucceeded = true;
            }
        }
        return anySucceeded;
    }

    /**
     * Convenience: find all relics in the player's hand and dispatch.
     */
    public void dispatchFromHand(Player player, TriggerType type, Event event) {
        ItemStack item = player.getInventory().getItemInMainHand();
        RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(item);
        if (relic != null) dispatch(player, item, relic, type, event);
    }

    /**
     * Dispatch checking all held + armor slots for passive relics.
     */
    public void dispatchFromAllSlots(Player player, TriggerType type, Event event) {
        RelicManager rm = plugin.getRelicManager();

        ItemStack main = player.getInventory().getItemInMainHand();
        RelicDefinition mainRelic = rm.getRelicFromItem(main);
        if (mainRelic != null) dispatch(player, main, mainRelic, type, event);

        ItemStack off = player.getInventory().getItemInOffHand();
        RelicDefinition offRelic = rm.getRelicFromItem(off);
        if (offRelic != null && !offRelic.equals(mainRelic)) {
            dispatch(player, off, offRelic, type, event);
        }

        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) continue;
            RelicDefinition armorRelic = rm.getRelicFromItem(armor);
            if (armorRelic != null) dispatch(player, armor, armorRelic, type, event);
        }
    }
}
