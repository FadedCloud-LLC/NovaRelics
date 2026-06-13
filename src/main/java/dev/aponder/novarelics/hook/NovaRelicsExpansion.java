package dev.aponder.novarelics.hook;

import dev.aponder.novarelics.NovaRelics;
import dev.aponder.novarelics.relic.RelicDefinition;
import dev.aponder.novarelics.storage.model.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NovaRelicsExpansion extends PlaceholderExpansion {

    private final NovaRelics plugin;

    public NovaRelicsExpansion(NovaRelics plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "novarelics"; }
    @Override public @NotNull String getAuthor() { return "APonder"; }
    @Override public @NotNull String getVersion() { return plugin.getDescription().getVersion(); }
    @Override public boolean persist() { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        return switch (params.toLowerCase()) {
            case "relic_count" -> String.valueOf(plugin.getRelicManager().getCount());

            case "discovered" -> {
                PlayerData data = plugin.getStorageManager().getPlayer(player.getUniqueId());
                yield String.valueOf(data.getDiscoveredCount());
            }

            case "equipped" -> {
                ItemStack main = player.getInventory().getItemInMainHand();
                RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(main);
                yield relic != null ? relic.getDisplayName() : "None";
            }

            case "rarity" -> {
                ItemStack main = player.getInventory().getItemInMainHand();
                RelicDefinition relic = plugin.getRelicManager().getRelicFromItem(main);
                yield relic != null ? relic.getRarity() : "None";
            }

            default -> {
                if (params.startsWith("cooldown_")) {
                    String relicId = params.substring("cooldown_".length());
                    RelicDefinition relic = plugin.getRelicManager().getRelic(relicId);
                    if (relic == null) yield "0";
                    long remaining = plugin.getCooldownManager()
                            .getRemainingSeconds(player, relic);
                    yield String.valueOf(remaining);
                }
                yield null;
            }
        };
    }
}
