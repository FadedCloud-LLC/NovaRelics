package dev.aponder.novarelics.aura;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.aura.impl.*;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AuraManager {

    private final NovaRelics plugin;
    private final Map<AuraType, AuraRenderer> renderers = new EnumMap<>(AuraType.class);
    private BukkitTask task;

    // Phase accumulator for smooth animation (ticks since start)
    private long tick = 0;

    public AuraManager(NovaRelics plugin) {
        this.plugin = plugin;
        registerRenderers();
    }

    private void registerRenderers() {
        renderers.put(AuraType.SPIRAL_EFFECT,       new SpiralAuraRenderer());
        renderers.put(AuraType.DOUBLE_SPIRAL_EFFECT, new DoubleSpiralAuraRenderer());
        renderers.put(AuraType.CIRCLE_EFFECT,        new CircleAuraRenderer());
        renderers.put(AuraType.GROUND_RING,          new GroundRingAuraRenderer());
        renderers.put(AuraType.PULSE_EFFECT,         new PulseAuraRenderer());
        renderers.put(AuraType.MAGIC_EFFECT,         new MagicAuraRenderer());
        renderers.put(AuraType.FIRE_EFFECT,          new FireAuraRenderer());
        renderers.put(AuraType.ICE_EFFECT,           new IceAuraRenderer());
        renderers.put(AuraType.NATURE_EFFECT,        new NatureAuraRenderer());
        renderers.put(AuraType.SHADOW_EFFECT,        new ShadowAuraRenderer());
        renderers.put(AuraType.LIGHTNING_EFFECT,     new LightningAuraRenderer());
        renderers.put(AuraType.CELESTIAL_EFFECT,     new CelestialAuraRenderer());
        renderers.put(AuraType.VOID_EFFECT,          new VoidAuraRenderer());
        renderers.put(AuraType.TOTEM_EFFECT,         new TotemAuraRenderer());
        // Fallback for unregistered types
        renderers.put(AuraType.CUSTOM_PARTICLE_AURA, new CircleAuraRenderer());
        renderers.put(AuraType.CUSTOM,               new CircleAuraRenderer());
    }

    public void start() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("aura.enabled", true)) return;

        int defaultInterval = plugin.getConfigManager().getMainConfig()
                .getInt("aura.global-update-ticks", 4);

        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick++;
                processAuras();
            }
        }.runTaskTimer(plugin, defaultInterval, defaultInterval);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    private void processAuras() {
        if (!plugin.getConfigManager().getMainConfig().getBoolean("aura.enabled", true)) return;

        double renderDistSq = getConfiguredRenderDistanceSq();
        List<String> disabledWorlds = plugin.getConfigManager().getMainConfig()
                .getStringList("performance.aura-disabled-worlds");
        int maxParticles = plugin.getConfigManager().getMainConfig()
                .getInt("performance.max-particles-per-player", 80);
        boolean chunkAware = plugin.getConfigManager().getMainConfig()
                .getBoolean("aura.chunk-aware", true);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (disabledWorlds.contains(player.getWorld().getName())) continue;

            // Collect all relics with auras for this player
            Collection<RelicDefinition> activeRelics = getActiveRelicsWithAura(player);
            if (activeRelics.isEmpty()) continue;

            Location playerLoc = player.getLocation();
            if (chunkAware && !ParticleUtil.isChunkLoaded(playerLoc)) continue;

            for (RelicDefinition relic : activeRelics) {
                AuraDefinition aura = relic.getAura();
                if (!aura.isEnabled()) continue;

                // Skip if not matching update interval
                if (aura.getUpdateTicks() > 0 && tick % aura.getUpdateTicks() != 0) continue;

                // Only render if nearby players exist within render distance
                boolean hasNearbyViewer = player.getWorld().getPlayers().stream()
                        .anyMatch(p -> p.getLocation().distanceSquared(playerLoc) <= renderDistSq);
                if (!hasNearbyViewer) continue;

                AuraRenderer renderer = renderers.getOrDefault(aura.getType(),
                        renderers.get(AuraType.CIRCLE_EFFECT));
                if (renderer != null) {
                    renderer.render(player, playerLoc, aura, tick, plugin);
                }
            }
        }
    }

    private Collection<RelicDefinition> getActiveRelicsWithAura(Player player) {
        java.util.List<RelicDefinition> result = new java.util.ArrayList<>();
        checkSlot(player.getInventory().getItemInMainHand(), result, false, player);
        checkSlot(player.getInventory().getItemInOffHand(), result, true, player);
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            checkSlot(armor, result, true, player);
        }
        return result;
    }

    private void checkSlot(ItemStack item, java.util.List<RelicDefinition> result,
                            boolean isPassiveSlot, Player player) {
        if (item == null || item.getType().isAir()) return;
        RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(item);
        if (relic == null) return;
        AuraDefinition aura = relic.getAura();
        if (!aura.isEnabled()) return;
        if (aura.isOnlyWhenHeld() && isPassiveSlot) return;
        result.add(relic);
    }

    private double getConfiguredRenderDistanceSq() {
        double dist = plugin.getConfigManager().getMainConfig()
                .getDouble("aura.render-distance", 32);
        return dist * dist;
    }

    public long getTick() { return tick; }
}
